using System;
using System.Collections.Generic;

namespace SmartMenza.Data.Repositories.Interfaces
{
    public class MealStatsRow
    {
        public int MealId { get; set; }
        public string MealName { get; set; } = string.Empty;

        public int MealTypeId { get; set; }
        public string MealTypeName { get; set; } = string.Empty;

        public int RatingsCount { get; set; }
        public decimal AverageRating { get; set; }
    }

    public interface IStatisticsRepository
    {
        List<MealStatsRow> GetMealStats(DateOnly? date, int? mealTypeId);
        (int TotalMeals, int TotalRatings, decimal OverallAverage) GetSummary(DateOnly? date, int? mealTypeId);
        (int TotalMeals, int TotalRatings, decimal OverallAverage, decimal MaxRating) GetOverallStats(DateOnly? dateFrom, DateOnly? dateTo);
    }
}