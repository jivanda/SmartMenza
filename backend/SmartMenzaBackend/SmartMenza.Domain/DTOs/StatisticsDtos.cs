using System;
using System.Collections.Generic;

namespace SmartMenza.Domain.DTOs
{
    public class MealStatsItemDto
    {
        public int MealId { get; set; }
        public string MealName { get; set; } = string.Empty;

        public int MealTypeId { get; set; }
        public string MealTypeName { get; set; } = string.Empty;

        public int RatingsCount { get; set; }
        public decimal AverageRating { get; set; }
    }

    public class StatsSummaryDto
    {
        public string? Date { get; set; }
        public int? MealTypeId { get; set; }

        public int TotalMeals { get; set; }
        public int TotalRatings { get; set; }
        public decimal OverallAverageRating { get; set; }

        public List<MealStatsItemDto> TopMeals { get; set; } = new();
    }
}