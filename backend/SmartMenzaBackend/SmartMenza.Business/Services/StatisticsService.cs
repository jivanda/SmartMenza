using System;
using System.Collections.Generic;
using System.Linq;
using SmartMenza.Data.Repositories.Interfaces;
using SmartMenza.Domain.DTOs;

namespace SmartMenza.Business.Services
{
    public class StatisticsService
    {
        private readonly IStatisticsRepository _repo;

        public StatisticsService(IStatisticsRepository repo)
        {
            _repo = repo;
        }

        public List<MealStatsItemDto> GetTopMeals(DateOnly? date, int? mealTypeId, string sortBy, int limit)
        {
            if (limit <= 0) limit = 10;

            var list = _repo.GetMealStats(date, mealTypeId);

            var sorted = sortBy?.ToLower() == "avg"
                ? list.OrderByDescending(x => x.AverageRating).ThenByDescending(x => x.RatingsCount)
                : list.OrderByDescending(x => x.RatingsCount).ThenByDescending(x => x.AverageRating);

            return sorted
                .Take(limit)
                .Select(x => new MealStatsItemDto
                {
                    MealId = x.MealId,
                    MealName = x.MealName,
                    MealTypeId = x.MealTypeId,
                    MealTypeName = x.MealTypeName,
                    RatingsCount = x.RatingsCount,
                    AverageRating = x.AverageRating
                })
                .ToList();
        }

        public StatsSummaryDto GetSummary(DateOnly? date, int? mealTypeId, string sortBy, int limit)
        {
            var (totalMeals, totalRatings, overallAvg) = _repo.GetSummary(date, mealTypeId);

            return new StatsSummaryDto
            {
                Date = date?.ToString("yyyy-MM-dd"),
                MealTypeId = mealTypeId,
                TotalMeals = totalMeals,
                TotalRatings = totalRatings,
                OverallAverageRating = overallAvg,
                TopMeals = GetTopMeals(date, mealTypeId, sortBy, limit)
            };
        }
        public OverallStatsDto GetOverallStats(DateOnly? dateFrom, DateOnly? dateTo)
        {
            var (totalMeals, totalRatings, overallAvg, maxRating) = _repo.GetOverallStats(dateFrom, dateTo);

            return new OverallStatsDto
            {
                DateFrom = dateFrom?.ToString("yyyy-MM-dd"),
                DateTo = dateTo?.ToString("yyyy-MM-dd"),
                TotalMeals = totalMeals,
                OverallAverageRating = overallAvg,
                MaxRating = maxRating
            };
        }
    }
}