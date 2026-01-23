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
using OpenAI; // OpenAI-DotNet official SDK
using OpenAI.Chat; // ChatClient, ChatMessage, ChatCompletion, ChatMessageContentPart

namespace SmartMenza.Business.Services
{
    public class MealRecommendationService
    {
        private readonly ChatClient _chatClient;
        private readonly MenuService _menuService;
        private readonly IConfiguration _configuration;
        private readonly ILogger<MealRecommendationService> _logger;

        private readonly string _deploymentName;

        public MealRecommendationService(
            ChatClient chatClient,
            MenuService menuService,
            IConfiguration configuration,
            ILogger<MealRecommendationService> logger)
        {
            _chatClient = chatClient ?? throw new ArgumentNullException(nameof(chatClient));
            _menuService = menuService ?? throw new ArgumentNullException(nameof(menuService));
            _configuration = configuration ?? throw new ArgumentNullException(nameof(configuration));
            _logger = logger ?? throw new ArgumentNullException(nameof(logger));

            _deploymentName = _configuration["AzureOpenAI:DeploymentName"] ?? "Meal_Reccomender_AI";
        }

        /// <summary>
        /// Recommend a single meal ID for a specific menu id (legacy support).
        /// This method builds a meals list from the specific menu and delegates to the shared worker.
        /// It preserves previous behavior but reuses the same deterministic call path.
        /// </summary>
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

            return await RecommendFromMealDtosAsync(meals, cancellationToken).ConfigureAwait(false);
        }

        /// <summary>
        /// Recommend a single meal ID for a given date.
        /// Uses MenuService -> MenuRepository.GetMenusByDate to retrieve menus for the date,
        /// collects all meals available that day and sends ONLY the meals array to the model.
        /// </summary>
        public async Task<int> RecommendMealForDateAsync(DateOnly date, CancellationToken cancellationToken = default)
        {
            var menus = _menuService.GetMenusByDate(date);
            if (menus == null || !menus.Any())
            {
                _logger.LogWarning("No menus found for date {Date}.", date.ToString("yyyy-MM-dd"));
                throw new InvalidOperationException("No menus found for the requested date.");
            }

            // Collect meals across all menus for the requested date.
            var allMeals = menus
                .SelectMany(md => md.Meals ?? new List<MealDto>())
                .DistinctBy(m => m.MealId)
                .ToList();

            if (!allMeals.Any())
            {
                _logger.LogWarning("Menus for date {Date} contained no meals.", date.ToString("yyyy-MM-dd"));
                throw new InvalidOperationException("No meals available for the requested date.");
            }

            // Send only the meals array (as required) to the AI and parse response.
            return await RecommendFromMealDtosAsync(allMeals, cancellationToken).ConfigureAwait(false);
        }

        /// <summary>
        /// Accepts multiple menus (e.g. all menus for a date), collects every meal present in them,
        /// removes duplicates by MealId and sends the combined meals array to the AI.
        /// </summary>
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
            return await RecommendFromMealDtosAsync(allMeals, cancellationToken).ConfigureAwait(false);
        }

        /// <summary>
        /// Shared worker: serializes provided MealDto list, calls Azure OpenAI chat completion,
        /// validates returned ID is in the provided list and returns it.
        /// </summary>
        private async Task<int> RecommendFromMealDtosAsync(List<MealDto> meals, CancellationToken cancellationToken)
        {
            if (meals == null || meals.Count == 0)
                throw new ArgumentException("meals must contain at least one item.", nameof(meals));

            // Serialize meals to JSON exactly as requested (no renaming).
            var jsonOptions = new JsonSerializerOptions { PropertyNamingPolicy = null, WriteIndented = false };
            var mealsJson = JsonSerializer.Serialize(meals, jsonOptions);

            // System prompt (exact)
            var systemPrompt = "You are a meal selection engine.\nYou will receive a JSON array of meals.\nSelect exactly one meal ID from the list.\nRespond with the ID only.\nDo not explain your choice.";

            // Build chat messages: system + user (user contains ONLY the JSON)
            var messages = new List<ChatMessage>
            {
                ChatMessage.CreateSystemMessage(systemPrompt),
                ChatMessage.CreateUserMessage(mealsJson)
            };

            _logger.LogDebug("Requesting meal selection from ChatClient (deployment {Deployment}) for {Count} meals.", _deploymentName, meals.Count);

            ChatCompletion completion;
            try
            {
                // The ChatClient registered in Program.cs is configured for the deployment/model.
                // Use the client to perform a chat completion. The concrete SDK exposes
                // a synchronous CompleteChat(...) and/or async CompleteChatAsync(...) - call the sync method
                // on a background thread to avoid blocking if async overload is not available.
                completion = await Task.Run(() => _chatClient.CompleteChat(messages), cancellationToken).ConfigureAwait(false);
            }
            catch (Exception ex)
            {
                // Avoid logging secrets; log message only.
                _logger.LogError(ex, "Chat completion call failed.");
                throw new InvalidOperationException("Failed to call AI service.", ex);
            }

            if (completion == null)
            {
                _logger.LogError("Chat completion response was null.");
                throw new InvalidOperationException("Invalid chat completion response.");
            }

            // Aggregate text parts if present. ChatCompletion does not have a 'Message' property in this SDK.
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
                // Last-resort fallback: use ChatCompletion.ToString() if the SDK serializes text there.
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
    }
}