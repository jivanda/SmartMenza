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

        /// <summary>
        /// Input: menuId
        /// - collect all meal names from the menu
        /// - send names as JSON to the AI for analysis
        /// - if AI returns valid numeric nutrition object, return it
        /// - otherwise fallback to summing available local nutrition fields from meals
        /// </summary>
        public async Task<NutritionResultDto> AnalyzeMenuByNamesAsync(int menuId, CancellationToken cancellationToken = default)
        {
            var menu = _menuService.GetMenuById(menuId)
                       ?? throw new InvalidOperationException($"Menu {menuId} not found.");

            var meals = menu.Meals ?? new List<MealDto>();

            // Build names-only payload
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

            // Aggregate text parts (SDK may provide content parts)
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

            // Parse expected JSON object with numeric fields
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

        // Fallback: sum available local nutrition values (missing values treated as 0)
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

        // Helper: try to read numeric value (as decimal) from a JsonElement property.
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