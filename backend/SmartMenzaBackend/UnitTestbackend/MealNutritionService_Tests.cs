using SmartMenza.Business.Services;
using SmartMenza.Domain.DTOs;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Text.Json;
using System.Threading.Tasks;
using Xunit;

namespace UnitTestbackend
{
    public class MealNutritionService_Tests
    {
        private static object InvokePrivateSumLocalNutrition(IEnumerable<MealDto> meals)
        {
            var t = typeof(MealNutritionService);
            var mi = t.GetMethod("SumLocalNutrition", BindingFlags.NonPublic | BindingFlags.Static)
                ?? throw new InvalidOperationException("SumLocalNutrition not found.");
            return mi.Invoke(null, new object?[] { meals! })!;
        }

        private static (bool success, decimal value) InvokeTryGetNumber(string json, string prop)
        {
            using var doc = JsonDocument.Parse(json);
            var root = doc.RootElement;

            var t = typeof(MealNutritionService);
            var mi = t.GetMethod("TryGetNumber", BindingFlags.NonPublic | BindingFlags.Static)
                ?? throw new InvalidOperationException("TryGetNumber not found.");

            object?[] parameters = new object?[] { root, prop, 0m };
            var ok = (bool)mi.Invoke(null, parameters)!;
            var val = (decimal)parameters[2]!;
            return (ok, val);
        }

        [Fact]
        public void SumLocalNutrition_SumsAllProvidedValues()
        {
            var meals = new List<MealDto>
            {
                new MealDto { MealId = 1, Calories = 100m, Protein = 10m, Carbohydrates = 20m, Fat = 5m },
                new MealDto { MealId = 2, Calories = 200m, Protein = 15m, Carbohydrates = 10m, Fat = 2m },
                new MealDto { MealId = 3, Calories = null, Protein = 5m, Carbohydrates = null, Fat = 1m }
            };

            var resultObj = InvokePrivateSumLocalNutrition(meals);
            var dto = resultObj as NutritionResultDto;
            Assert.NotNull(dto);
            Assert.Equal(300m, dto!.Calories);
            Assert.Equal(30m, dto.Proteins);
            Assert.Equal(30m, dto.Carbohydrates);
            Assert.Equal(8m, dto.Fats);
        }

        [Fact]
        public void TryGetNumber_ParsesNumericAndStringValues()
        {
            var json = "{\"calories\": 123.45, \"proteins\": \"10.5\" }";

            var (okCalories, calories) = InvokeTryGetNumber(json, "calories");
            Assert.True(okCalories);
            Assert.Equal(123.45m, calories);

            var (okProteins, proteins) = InvokeTryGetNumber(json, "proteins");
            Assert.True(okProteins);
            Assert.Equal(10.5m, proteins);
        }

        [Fact]
        public void TryGetNumber_ReturnsFalse_ForMissingOrInvalid()
        {
            var json = "{\"calories\": \"notanumber\" }";

            var (okCalories, _) = InvokeTryGetNumber(json, "missing");
            Assert.False(okCalories);

            var (okInvalid, _) = InvokeTryGetNumber(json, "calories");
            Assert.False(okInvalid);
        }
    }
}