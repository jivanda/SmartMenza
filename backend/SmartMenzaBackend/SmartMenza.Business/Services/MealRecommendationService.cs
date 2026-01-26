using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Text.Json;
using System.Text.RegularExpressions;
using System.Threading;
using System.Threading.Tasks;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Logging;
using SmartMenza.Domain.DTOs;
using SmartMenza.Data.Entities;
using OpenAI;
using OpenAI.Chat;

namespace SmartMenza.Business.Services
{
    public class MealRecommendationService
    {
        private readonly ChatClient _chatClient;
        private readonly MenuService _menuService;
        private readonly GoalService _goalService;
        private readonly IConfiguration _configuration;
        private readonly ILogger<MealRecommendationService> _logger;

        private readonly string _deploymentName;

        public MealRecommendationService(
            ChatClient chatClient,
            MenuService menuService,
            GoalService goalService,
            IConfiguration configuration,
            ILogger<MealRecommendationService> logger)
        {
            _chatClient = chatClient ?? throw new ArgumentNullException(nameof(chatClient));
            _menuService = menuService ?? throw new ArgumentNullException(nameof(menuService));
            _goalService = goalService ?? throw new ArgumentNullException(nameof(goalService));
            _configuration = configuration ?? throw new ArgumentNullException(nameof(configuration));
            _logger = logger ?? throw new ArgumentNullException(nameof(logger));

            _deploymentName = _configuration["AzureOpenAI:DeploymentName"] ?? "Meal_Reccomender_AI";
        }

        public async Task<int> RecommendMealForMenuAsync(int menuId, CancellationToken cancellationToken = default)
        {
            var menu = _menuService.GetMenuById(menuId);
            if (menu == null)
            {
                _logger.LogWarning("Menu {MenuId} not found.", menuId);
                throw new InvalidOperationException("Menu not found.");
            }

            var meals = menu.Meals;
            if (meals == null || !meals.Any())
            {
                _logger.LogWarning("Menu {MenuId} contains no meals.", menuId);
                throw new InvalidOperationException("Menu contains no meals.");
            }

            return await RecommendFromMealDtosAsync(meals, null, cancellationToken).ConfigureAwait(false);
        }

        public async Task<int> RecommendMealForDateAsync(DateOnly date, int? userId = null, CancellationToken cancellationToken = default)
        {
            var menus = _menuService.GetMenusByDate(date);
            if (menus == null || !menus.Any())
            {
                _logger.LogWarning("No menus found for date {Date}.", date.ToString("yyyy-MM-dd"));
                throw new InvalidOperationException("No menus found for the requested date.");
            }

            var allMeals = menus
                .SelectMany(md => md.Meals ?? new List<MealDto>())
                .DistinctBy(m => m.MealId)
                .ToList();

            if (!allMeals.Any())
            {
                _logger.LogWarning("Menus for date {Date} contained no meals.", date.ToString("yyyy-MM-dd"));
                throw new InvalidOperationException("No meals available for the requested date.");
            }

            return await RecommendFromMealDtosAsync(allMeals, userId, cancellationToken).ConfigureAwait(false);
        }

        public async Task<int> RecommendMealForMenusAsync(IEnumerable<MenuResponseDto> menus, CancellationToken cancellationToken = default)
        {
            if (menus == null) throw new ArgumentNullException(nameof(menus));

            var menuList = menus.ToList();
            if (!menuList.Any())
            {
                _logger.LogWarning("No menus provided to RecommendMealForMenusAsync.");
                throw new InvalidOperationException("No menus provided.");
            }

            var allMeals = menuList
                .SelectMany(m => m.Meals ?? new List<MealDto>())
                .DistinctBy(m => m.MealId)
                .ToList();

            if (!allMeals.Any())
            {
                _logger.LogWarning("Provided menus contained no meals.");
                throw new InvalidOperationException("No meals available in provided menus.");
            }

            _logger.LogDebug("Collected {Count} distinct meals from {MenuCount} menus.", allMeals.Count, menuList.Count);
            return await RecommendFromMealDtosAsync(allMeals, null, cancellationToken).ConfigureAwait(false);
        }

        private async Task<int> RecommendFromMealDtosAsync(List<MealDto> meals, int? userId, CancellationToken cancellationToken)
        {
            if (meals == null || meals.Count == 0)
                throw new ArgumentException("meals must contain at least one item.", nameof(meals));

            var jsonOptions = new JsonSerializerOptions { PropertyNamingPolicy = null, WriteIndented = false };

            NutritionGoal? activeGoal = null;
            if (userId.HasValue)
            {
                try
                {
                    var goals = _goalService.GetGoalsByUser(userId.Value);
                    activeGoal = goals
                        .OrderByDescending(g => g.DateSet)
                        .FirstOrDefault();
                }
                catch (Exception ex)
                {
                    _logger.LogWarning(ex, "Failed to retrieve goals for user {UserId}. Proceeding without goal.", userId.Value);
                    activeGoal = null;
                }
            }

            object payload;
            if (activeGoal != null)
            {
                payload = new
                {
                    meals = meals,
                    goal = new
                    {
                        calories = activeGoal.Calories,
                        protein = activeGoal.Protein,
                        carbohydrates = activeGoal.Carbohydrates,
                        fat = activeGoal.Fat,
                        dateSet = activeGoal.DateSet
                    }
                };
            }
            else
            {
                payload = new
                {
                    meals = meals
                };
            }

            var payloadJson = JsonSerializer.Serialize(payload, jsonOptions);

            var systemPrompt = "You are a meal selection engine.\nYou will receive a JSON object with a 'meals' array and an optional 'goal' object.\nSelect exactly one meal ID from the list that best matches the user's nutritional goal if provided.\nRespond with the ID only.\nDo not explain your choice.";

            var messages = new List<ChatMessage>
            {
                ChatMessage.CreateSystemMessage(systemPrompt),
                ChatMessage.CreateUserMessage(payloadJson)
            };

            _logger.LogDebug("Requesting meal selection from ChatClient (deployment {Deployment}) for {Count} meals. UserId: {UserId}", _deploymentName, meals.Count, userId?.ToString() ?? "none");

            ChatCompletion completion;
            try
            {
                completion = await Task.Run(() => _chatClient.CompleteChat(messages), cancellationToken).ConfigureAwait(false);
            }
            catch (Exception ex)
            {
                _logger.LogWarning(ex, "Chat completion call failed. Attempting local fallback recommendation.");

                var message = ex?.Message ?? string.Empty;
                var isAuthError = message.Contains("401") || message.IndexOf("unauthorized", StringComparison.OrdinalIgnoreCase) >= 0;

                if (isAuthError)
                {
                    var fallbackId = RecommendClosestMealToGoal(meals, activeGoal, cancellationToken);
                    _logger.LogInformation("Fallback selected meal id {Id} due to AI unavailability.", fallbackId);
                    return fallbackId;
                }

                _logger.LogError(ex, "Chat completion call failed.");
                throw new InvalidOperationException("Failed to call AI service.", ex);
            }

            if (completion == null)
            {
                _logger.LogError("Chat completion response was null.");
                throw new InvalidOperationException("Invalid chat completion response.");
            }

            var contentBuilder = new StringBuilder();
            if (completion.Content != null && completion.Content.Any())
            {
                foreach (var part in completion.Content)
                {
                    if (!string.IsNullOrEmpty(part.Text))
                        contentBuilder.Append(part.Text);
                }
            }
            else
            {
                var fallback = completion.ToString();
                if (!string.IsNullOrWhiteSpace(fallback))
                    contentBuilder.Append(fallback);
            }

            var content = contentBuilder.ToString().Trim();
            _logger.LogDebug("Raw model output: {Output}", content);

            var parsedId = ParseSingleIntegerFromText(content);
            if (!parsedId.HasValue)
            {
                _logger.LogError("Failed to parse integer id from model output.");
                throw new InvalidOperationException("Model did not return a valid integer ID.");
            }

            var chosenId = parsedId.Value;
            var availableIds = new HashSet<int>(meals.Select(m => m.MealId));
            if (!availableIds.Contains(chosenId))
            {
                _logger.LogError("Model returned id {Id} which is not present in input meal ids.", chosenId);
                throw new InvalidOperationException("Model returned an ID that does not exist in provided meals.");
            }

            _logger.LogInformation("Model selected meal id {Id}.", chosenId);
            return chosenId;
        }

        private static int? ParseSingleIntegerFromText(string text)
        {
            if (string.IsNullOrWhiteSpace(text)) return null;
            var m = Regex.Match(text, @"-?\d+");
            if (!m.Success) return null;
            if (int.TryParse(m.Value, out var id)) return id;
            return null;
        }

        private int RecommendClosestMealToGoal(List<MealDto> meals, NutritionGoal? goal, CancellationToken cancellationToken)
        {
            if (meals == null || meals.Count == 0)
                throw new ArgumentException("meals must contain at least one item.", nameof(meals));

            if (goal == null)
            {
                var chosen = meals
                    .OrderByDescending(m =>
                        (m.Calories.HasValue ? 1 : 0) +
                        (m.Protein.HasValue ? 1 : 0) +
                        (m.Carbohydrates.HasValue ? 1 : 0) +
                        (m.Fat.HasValue ? 1 : 0))
                    .ThenBy(m => m.MealId)
                    .First();

                return chosen.MealId;
            }

            decimal wCalories = 0.5m, wProtein = 0.25m, wCarbs = 0.15m, wFat = 0.10m;

            decimal bestScore = decimal.MaxValue;
            MealDto? bestMeal = null;

            foreach (var meal in meals)
            {
                if (cancellationToken.IsCancellationRequested)
                    throw new OperationCanceledException(cancellationToken);

                var scCalories = RelativeDifference(meal.Calories, goal.Calories);
                var scProtein = RelativeDifference(meal.Protein, goal.Protein);
                var scCarbs = RelativeDifference(meal.Carbohydrates, goal.Carbohydrates);
                var scFat = RelativeDifference(meal.Fat, goal.Fat);

                var score = wCalories * scCalories + wProtein * scProtein + wCarbs * scCarbs + wFat * scFat;

                var completeness =
                    (meal.Calories.HasValue ? 1 : 0) +
                    (meal.Protein.HasValue ? 1 : 0) +
                    (meal.Carbohydrates.HasValue ? 1 : 0) +
                    (meal.Fat.HasValue ? 1 : 0);

                var adjustedScore = score - (0.0001m * completeness);

                if (adjustedScore < bestScore || (adjustedScore == bestScore && (bestMeal == null || meal.MealId < bestMeal.MealId)))
                {
                    bestScore = adjustedScore;
                    bestMeal = meal;
                }
            }

            if (bestMeal == null)
                bestMeal = meals.First();

            return bestMeal.MealId;
        }

        private static decimal RelativeDifference(decimal? mealVal, decimal goalVal)
        {
            if (!mealVal.HasValue) return 1.0m;
            var denom = Math.Max(goalVal, 1.0m);
            var diff = Math.Abs(mealVal.Value - goalVal) / denom;
            return Math.Min(diff, 10.0m);
        }
    }
}