using SmartMenza.Business.Services;
using SmartMenza.Data.Entities;
using SmartMenza.Domain.DTOs;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using System.Runtime.Serialization;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using Xunit;

namespace UnitTestbackend
{
    public class MealRecommendationService_Tests
    {
        private static object? InvokePrivateStaticParse(string input)
        {
            var t = typeof(MealRecommendationService);
            var mi = t.GetMethod("ParseSingleIntegerFromText", BindingFlags.NonPublic | BindingFlags.Static)
                ?? throw new InvalidOperationException("ParseSingleIntegerFromText not found.");
            return mi.Invoke(null, new object?[] { input });
        }

        private static int InvokePrivateRecommendClosest(IList<MealDto> meals, NutritionGoal? goal)
        {
            var t = typeof(MealRecommendationService);
            var mi = t.GetMethod("RecommendClosestMealToGoal", BindingFlags.NonPublic | BindingFlags.Instance)
                ?? throw new InvalidOperationException("RecommendClosestMealToGoal not found.");

            var svc = (MealRecommendationService)FormatterServices.GetUninitializedObject(typeof(MealRecommendationService))!;

            var result = mi.Invoke(svc, new object?[] { meals, goal, CancellationToken.None });
            return Convert.ToInt32(result);
        }

        [Fact]
        public void ParseSingleIntegerFromText_ReturnsInt_WhenNumberPresent()
        {
            var parsed = InvokePrivateStaticParse("Selected id:  42  ");
            Assert.NotNull(parsed);
            Assert.Equal(42, Convert.ToInt32(parsed));
        }

        [Fact]
        public void ParseSingleIntegerFromText_ReturnsNull_WhenNoNumber()
        {
            var parsed = InvokePrivateStaticParse("No numbers here");
            Assert.Null(parsed);
        }

        [Fact]
        public void RecommendClosestMealToGoal_SelectsMealWithMostCompleteness_WhenNoGoal()
        {
            var meals = new List<MealDto>
            {
                new MealDto { MealId = 1, Name = "A", Calories = null, Protein = null, Carbohydrates = null, Fat = null },
                new MealDto { MealId = 2, Name = "B", Calories = 200m, Protein = null, Carbohydrates = null, Fat = null },
                new MealDto { MealId = 3, Name = "C", Calories = 100m, Protein = 10m, Carbohydrates = null, Fat = null }, // completeness 2
                new MealDto { MealId = 4, Name = "D", Calories = 100m, Protein = 10m, Carbohydrates = 5m, Fat = null }   // completeness 3 -> should be chosen
            };

            var chosen = InvokePrivateRecommendClosest(meals, null);
            Assert.Equal(4, chosen);
        }

        [Fact]
        public void RecommendClosestMealToGoal_WithGoal_SelectsClosestByWeightedScore()
        {
            var meals = new List<MealDto>
            {
                new MealDto { MealId = 10, Name = "LowCal", Calories = 300m, Protein = 10m, Carbohydrates = 20m, Fat = 5m },
                new MealDto { MealId = 11, Name = "Match",   Calories = 600m, Protein = 30m, Carbohydrates = 80m, Fat = 20m },
                new MealDto { MealId = 12, Name = "HighCal", Calories = 1200m, Protein = 50m, Carbohydrates = 120m, Fat = 50m }
            };

            var goal = new NutritionGoal
            {
                Calories = 650m,
                Protein = 35m,
                Carbohydrates = 80m,
                Fat = 20m
            };

            var chosen = InvokePrivateRecommendClosest(meals, goal);
            Assert.Equal(11, chosen);
        }
    }
}