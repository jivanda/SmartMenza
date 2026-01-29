using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Text.Json;
using System.Threading;
using System.Threading.Tasks;
using Microsoft.Extensions.Logging;
using SmartMenza.Domain.DTOs;
using OpenAI;
using OpenAI.Chat;

namespace SmartMenza.Business.Services
{
    public class MealNutritionService
    {
        private readonly ChatClient _chatClient;
        private readonly MenuService _menuService;
        private readonly ILogger<MealNutritionService> _logger;

        public MealNutritionService(
            ChatClient chatClient,
            MenuService menuService,
            ILogger<MealNutritionService> logger)
        {
            _chatClient = chatClient ?? throw new ArgumentNullException(nameof(chatClient));
            _menuService = menuService ?? throw new ArgumentNullException(nameof(menuService));
            _logger = logger ?? throw new ArgumentNullException(nameof(logger));
        }

        public async Task<NutritionResultDto> AnalyzeMenuByNamesAsync(int menuId, CancellationToken cancellationToken = default)
        {
            var menu = _menuService.GetMenuById(menuId)
                       ?? throw new InvalidOperationException($"Menu {menuId} not found.");

            var meals = menu.Meals ?? new List<MealDto>();

            var payload = new
            {
                menu.MenuId,
                menu.Name,
                meals = meals.Select(m => new { m.MealId, m.Name }).ToArray()
            };

            var jsonOptions = new JsonSerializerOptions { PropertyNamingPolicy = null, WriteIndented = false };
            var payloadJson = JsonSerializer.Serialize(payload, jsonOptions);

            var systemPrompt = new StringBuilder()
                .AppendLine("You are a nutrition calculation engine.")
                .AppendLine("You will receive a JSON object with menu metadata and an array 'meals' with objects containing 'mealId' and 'name'.")
                .AppendLine("Calculate total nutrition for the menu and RETURN ONLY a single JSON OBJECT with numeric fields:")
                .AppendLine("{\"calories\": number, \"proteins\": number, \"carbohydrates\": number, \"fats\": number}")
                .AppendLine("Do not add commentary, arrays, or extra fields.")
                .ToString();

            var messages = new List<ChatMessage>
            {
                ChatMessage.CreateSystemMessage(systemPrompt),
                ChatMessage.CreateUserMessage(payloadJson)
            };

            ChatCompletion completion;
            try
            {
                completion = await Task.Run(() => _chatClient.CompleteChat(messages), cancellationToken).ConfigureAwait(false);
            }
            catch (Exception ex)
            {
                _logger.LogWarning(ex, "AI call failed for menu {MenuId}. Falling back to local sum.", menuId);
                return SumLocalNutrition(meals);
            }

            if (completion == null)
            {
                _logger.LogWarning("AI returned null completion for menu {MenuId}. Falling back to local sum.", menuId);
                return SumLocalNutrition(meals);
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
                var fallbackText = completion.ToString();
                if (!string.IsNullOrWhiteSpace(fallbackText))
                    contentBuilder.Append(fallbackText);
            }

            var raw = contentBuilder.ToString().Trim();
            if (string.IsNullOrWhiteSpace(raw))
            {
                _logger.LogWarning("AI returned empty response for menu {MenuId}. Falling back to local sum.", menuId);
                return SumLocalNutrition(meals);
            }

            try
            {
                using var doc = JsonDocument.Parse(raw);
                if (doc.RootElement.ValueKind != JsonValueKind.Object)
                {
                    _logger.LogWarning("AI returned non-object JSON for menu {MenuId}. Falling back to local sum.", menuId);
                    return SumLocalNutrition(meals);
                }

                var root = doc.RootElement;

                if (!TryGetNumber(root, "calories", out var calories) &&
                    !TryGetNumber(root, "calorie", out calories))
                    return SumLocalNutrition(meals);

                if (!TryGetNumber(root, "proteins", out var proteins) &&
                    !TryGetNumber(root, "protein", out proteins))
                    return SumLocalNutrition(meals);

                if (!TryGetNumber(root, "carbohydrates", out var carbs) &&
                    !TryGetNumber(root, "carbs", out carbs))
                    return SumLocalNutrition(meals);

                if (!TryGetNumber(root, "fats", out var fats) &&
                    !TryGetNumber(root, "fat", out fats))
                    return SumLocalNutrition(meals);

                return new NutritionResultDto
                {
                    Calories = calories,
                    Proteins = proteins,
                    Carbohydrates = carbs,
                    Fats = fats
                };
            }
            catch (JsonException jex)
            {
                _logger.LogWarning(jex, "AI returned invalid JSON for menu {MenuId}. Falling back to local sum.", menuId);
                return SumLocalNutrition(meals);
            }
        }

        /// <summary>
        /// Ask the AI to assess a menu. AI must RETURN ONLY a single JSON OBJECT with fields:
        /// { "menuId": number, "reasoning": string }
        /// The service returns a strongly-typed DTO. On AI failure or invalid response a local heuristic is used.
        /// </summary>
        public async Task<NutritionAssessmentDto> AssessMenuHealthAsync(int menuId, CancellationToken cancellationToken = default)
        {
            var menu = _menuService.GetMenuById(menuId)
                       ?? throw new InvalidOperationException($"Menu {menuId} not found.");

            var meals = menu.Meals ?? new List<MealDto>();

            var payload = new
            {
                menuId = menu.MenuId,
                menuName = menu.Name,
                meals = meals.Select(m => new
                {
                    m.MealId,
                    m.Name,
                    calories = m.Calories,
                    protein = m.Protein,
                    carbohydrates = m.Carbohydrates,
                    fat = m.Fat
                }).ToArray()
            };

            var jsonOptions = new JsonSerializerOptions { PropertyNamingPolicy = null, WriteIndented = false };
            var payloadJson = JsonSerializer.Serialize(payload, jsonOptions);

            var systemPrompt = new StringBuilder()
                .AppendLine("You are a nutrition assessor.")
                .AppendLine("You will receive a JSON object with menuId, menuName and an array 'meals' where each meal may include numeric fields: calories, protein, carbohydrates, fat.")
                .AppendLine("Analyze the menu's total nutrition and macronutrient balance and RETURN ONLY a single JSON OBJECT with exactly these fields:")
                .AppendLine("{\"menuId\": number, \"reasoning\": string}")
                .AppendLine("The reasoning should be a concise human-readable explanation of the menu's nutritional status (mention totals / imbalances as needed).")
                .AppendLine("You the content of the reasoning field should be written in Croatian.")
                .AppendLine("Do not include extra fields, arrays, or commentary outside the JSON object.")
                .ToString();

            var messages = new List<ChatMessage>
            {
                ChatMessage.CreateSystemMessage(systemPrompt),
                ChatMessage.CreateUserMessage(payloadJson)
            };

            ChatCompletion completion;
            try
            {
                completion = await Task.Run(() => _chatClient.CompleteChat(messages), cancellationToken).ConfigureAwait(false);
            }
            catch (Exception ex)
            {
                _logger.LogWarning(ex, "AI assessment failed for menu {MenuId}. Falling back to local heuristic.", menuId);
                return BuildLocalAssessment(menuId, meals);
            }

            if (completion == null)
            {
                _logger.LogWarning("AI returned null completion for menu {MenuId}. Falling back to local heuristic.", menuId);
                return BuildLocalAssessment(menuId, meals);
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
                var fallbackText = completion.ToString();
                if (!string.IsNullOrWhiteSpace(fallbackText))
                    contentBuilder.Append(fallbackText);
            }

            var raw = contentBuilder.ToString().Trim();
            if (string.IsNullOrWhiteSpace(raw))
            {
                _logger.LogWarning("AI returned empty response for menu {MenuId}. Falling back to local heuristic.", menuId);
                return BuildLocalAssessment(menuId, meals);
            }

            try
            {
                using var doc = JsonDocument.Parse(raw);
                if (doc.RootElement.ValueKind != JsonValueKind.Object)
                {
                    _logger.LogWarning("AI returned non-object JSON for menu {MenuId}. Falling back to local heuristic.", menuId);
                    return BuildLocalAssessment(menuId, meals);
                }

                var root = doc.RootElement;

                // menuId check (optional)
                if (root.TryGetProperty("menuId", out var menuIdProp) && menuIdProp.ValueKind == JsonValueKind.Number)
                {
                    if (menuIdProp.TryGetInt32(out var returnedMenuId) && returnedMenuId != menuId)
                    {
                        _logger.LogWarning("AI returned mismatched menuId ({Returned}) for menu {Expected}. Falling back.", returnedMenuId, menuId);
                        return BuildLocalAssessment(menuId, meals);
                    }
                }

                if (!root.TryGetProperty("reasoning", out var reasoningProp) || reasoningProp.ValueKind != JsonValueKind.String)
                {
                    _logger.LogWarning("AI response missing or invalid 'reasoning' for menu {MenuId}. Falling back.", menuId);
                    return BuildLocalAssessment(menuId, meals);
                }

                var reasoning = reasoningProp.GetString() ?? string.Empty;

                return new NutritionAssessmentDto
                {
                    MenuId = menuId,
                    Reasoning = reasoning
                };
            }
            catch (JsonException jex)
            {
                _logger.LogWarning(jex, "AI returned invalid JSON for menu {MenuId}. Falling back to local heuristic.", menuId);
                return BuildLocalAssessment(menuId, meals);
            }
        }

        private static NutritionAssessmentDto BuildLocalAssessment(int menuId, IEnumerable<MealDto> meals)
        {
            var totals = SumLocalNutrition(meals);

            if (totals.Calories <= 0m)
            {
                return new NutritionAssessmentDto
                {
                    MenuId = menuId,
                    Reasoning = "Insufficient nutritional data for menu meals to assess healthiness."
                };
            }

            // Estimate macro calorie contributions
            var proteinCalories = totals.Proteins * 4m;
            var carbsCalories = totals.Carbohydrates * 4m;
            var fatsCalories = totals.Fats * 9m;
            var macroTotal = proteinCalories + carbsCalories + fatsCalories;

            var issues = new List<string>();

            if (totals.Calories < 400m)
                issues.Add($"Total calories ({totals.Calories}) are low.");
            else if (totals.Calories > 1200m)
                issues.Add($"Total calories ({totals.Calories}) are high.");

            if (macroTotal > 0m)
            {
                var pPct = proteinCalories / macroTotal * 100m;
                var cPct = carbsCalories / macroTotal * 100m;
                var fPct = fatsCalories / macroTotal * 100m;

                if (pPct < 10m || pPct > 35m) issues.Add($"Protein proportion ({Math.Round(pPct,1)}%) is outside recommended 10-35%.");
                if (cPct < 45m || cPct > 65m) issues.Add($"Carbohydrate proportion ({Math.Round(cPct,1)}%) is outside recommended 45-65%.");
                if (fPct < 20m || fPct > 35m) issues.Add($"Fat proportion ({Math.Round(fPct,1)}%) is outside recommended 20-35%.");
            }
            else
            {
                issues.Add("No macronutrient breakdown available to assess balance.");
            }

            var reasoning = issues.Count == 0
                ? $"Menu looks nutritionally reasonable. Totals: calories={totals.Calories}, proteins={totals.Proteins}, carbohydrates={totals.Carbohydrates}, fats={totals.Fats}."
                : $"Menu may be suboptimal: {string.Join(" ", issues)} Totals: calories={totals.Calories}, proteins={totals.Proteins}, carbohydrates={totals.Carbohydrates}, fats={totals.Fats}.";

            return new NutritionAssessmentDto
            {
                MenuId = menuId,
                Reasoning = reasoning
            };
        }

        private static NutritionResultDto SumLocalNutrition(IEnumerable<MealDto> meals)
        {
            decimal calories = 0m, proteins = 0m, carbs = 0m, fats = 0m;

            foreach (var m in meals)
            {
                if (m.Calories.HasValue) calories += m.Calories.Value;
                if (m.Protein.HasValue) proteins += m.Protein.Value;
                if (m.Carbohydrates.HasValue) carbs += m.Carbohydrates.Value;
                if (m.Fat.HasValue) fats += m.Fat.Value;
            }

            return new NutritionResultDto
            {
                Calories = calories,
                Proteins = proteins,
                Carbohydrates = carbs,
                Fats = fats
            };
        }

        private static bool TryGetNumber(JsonElement root, string propertyName, out decimal value)
        {
            value = 0m;
            if (!root.TryGetProperty(propertyName, out var prop)) return false;

            switch (prop.ValueKind)
            {
                case JsonValueKind.Number:
                    if (prop.TryGetDecimal(out var dec))
                    {
                        value = dec;
                        return true;
                    }
                    break;
                case JsonValueKind.String:
                    {
                        var s = prop.GetString();
                        if (decimal.TryParse(s, out var parsed))
                        {
                            value = parsed;
                            return true;
                        }
                        return false;
                    }
                default:
                    return false;
            }

            return false;
        }
    }
}