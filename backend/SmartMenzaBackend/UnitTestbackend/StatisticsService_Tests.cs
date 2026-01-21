using System;
using System.Collections.Generic;
using System.Linq;
using SmartMenza.Business.Services;
using SmartMenza.Data.Repositories.Interfaces;
using SmartMenza.Domain.DTOs;
using Xunit;

namespace UnitTestbackend
{
    public class StatisticsService_Tests
    {
        private class FakeStatsRepo : IStatisticsRepository
        {
            private readonly List<MealStatsRow> _rows;
            private readonly (int, int, decimal) _summary;

            public FakeStatsRepo(List<MealStatsRow> rows, (int, int, decimal) summary)
            {
                _rows = rows;
                _summary = summary;
            }

            public List<MealStatsRow> GetMealStats(DateOnly? date, int? mealTypeId)
            {
                return _rows.ToList();
            }

            public (int TotalMeals, int TotalRatings, decimal OverallAverage) GetSummary(DateOnly? date, int? mealTypeId)
            {
                return _summary;
            }
        }

        [Fact]
        public void GetTopMeals_SortsByCount_WhenSortByCount()
        {
            var rows = new List<MealStatsRow>
            {
                new MealStatsRow { MealId = 1, MealName = "A", MealTypeId = 1, MealTypeName = "X", RatingsCount = 5, AverageRating = 3.0m },
                new MealStatsRow { MealId = 2, MealName = "B", MealTypeId = 1, MealTypeName = "X", RatingsCount = 8, AverageRating = 2.0m },
                new MealStatsRow { MealId = 3, MealName = "C", MealTypeId = 1, MealTypeName = "X", RatingsCount = 5, AverageRating = 4.5m }
            };

            var repo = new FakeStatsRepo(rows, (3, 18, 3.166m));
            var svc = new StatisticsService(repo);

            var result = svc.GetTopMeals(null, null, "count", 10);

            Assert.Equal(new[] { 2, 3, 1 }, result.Select(r => r.MealId).ToArray());
        }

        [Fact]
        public void GetTopMeals_SortsByAvg_WhenSortByAvg()
        {
            var rows = new List<MealStatsRow>
            {
                new MealStatsRow { MealId = 1, MealName = "A", MealTypeId = 1, MealTypeName = "X", RatingsCount = 5, AverageRating = 3.0m },
                new MealStatsRow { MealId = 2, MealName = "B", MealTypeId = 1, MealTypeName = "X", RatingsCount = 8, AverageRating = 4.8m },
                new MealStatsRow { MealId = 3, MealName = "C", MealTypeId = 1, MealTypeName = "X", RatingsCount = 10, AverageRating = 4.8m }
            };

            var repo = new FakeStatsRepo(rows, (3, 23, 4.2m));
            var svc = new StatisticsService(repo);

            var result = svc.GetTopMeals(null, null, "avg", 10);

            Assert.Equal(new[] { 3, 2, 1 }, result.Select(r => r.MealId).ToArray());
        }

        [Fact]
        public void GetTopMeals_RespectsLimit()
        {
            var rows = Enumerable.Range(1, 20).Select(i =>
                new MealStatsRow { MealId = i, MealName = $"M{i}", MealTypeId = 1, MealTypeName = "X", RatingsCount = i, AverageRating = i % 5 + 1 }).ToList();

            var repo = new FakeStatsRepo(rows, (20, 210, 3.0m));
            var svc = new StatisticsService(repo);

            var result = svc.GetTopMeals(null, null, "count", 5);

            Assert.Equal(5, result.Count);
            Assert.Equal(new[] { 20, 19, 18, 17, 16 }, result.Select(r => r.MealId).ToArray());
        }

        [Fact]
        public void GetSummary_ReturnsSummaryAndTopMeals()
        {
            var rows = new List<MealStatsRow>
            {
                new MealStatsRow { MealId = 1, MealName = "A", MealTypeId = 2, MealTypeName = "T", RatingsCount = 2, AverageRating = 4.0m },
            };

            var summaryTuple = (1, 2, 4.0m);
            var repo = new FakeStatsRepo(rows, summaryTuple);
            var svc = new StatisticsService(repo);

            var date = DateOnly.FromDateTime(new DateTime(2025, 11, 3));
            var dto = svc.GetSummary(date, 2, "count", 10);

            Assert.Equal("2025-11-03", dto.Date);
            Assert.Equal(2, dto.MealTypeId);
            Assert.Equal(1, dto.TotalMeals);
            Assert.Equal(2, dto.TotalRatings);
            Assert.Equal(4.0m, dto.OverallAverageRating);
            Assert.Single(dto.TopMeals);
            Assert.Equal(1, dto.TopMeals[0].MealId);
        }
    }
}