    using System;
using System.Collections.Generic;
using System.Linq;
using Microsoft.EntityFrameworkCore;
using SmartMenza.Data.Context;
using SmartMenza.Data.Repositories.Interfaces;

namespace SmartMenza.Data.Repositories.Implementations
{
    public class StatisticsRepository : IStatisticsRepository
    {
        private readonly SmartMenzaContext _context;

        public StatisticsRepository(SmartMenzaContext context)
        {
            _context = context;
        }

        private IQueryable<int> MealIdsForDate(DateOnly date)
        {
            return (from md in _context.MenuDate.AsNoTracking()
                    join mm in _context.MenuMeal.AsNoTracking() on md.MenuId equals mm.MenuId
                    where md.Date == date
                    select mm.MealId).Distinct();
        }

        public List<MealStatsRow> GetMealStats(DateOnly? date, int? mealTypeId)
        {
            var meals = _context.Meal
                .AsNoTracking()
                .Include(m => m.MealType)
                .AsQueryable();

            if (mealTypeId.HasValue)
                meals = meals.Where(m => m.MealTypeId == mealTypeId.Value);

            if (date.HasValue)
            {
                var ids = MealIdsForDate(date.Value);
                meals = meals.Where(m => ids.Contains(m.MealId));
            }

            var stats =
                from m in meals
                join rc in _context.RatingComment.AsNoTracking()
                    on m.MealId equals rc.MealId into ratings
                select new MealStatsRow
                {
                    MealId = m.MealId,
                    MealName = m.Name,
                    MealTypeId = m.MealTypeId,
                    MealTypeName = m.MealType.Name,
                    RatingsCount = ratings.Count(),
                    AverageRating = ratings.Count() == 0 ? 0m : ratings.Average(x => (decimal)x.Rating)
                };

            return stats.ToList();
        }

        public (int TotalMeals, int TotalRatings, decimal OverallAverage) GetSummary(DateOnly? date, int? mealTypeId)
        {
            var meals = _context.Meal.AsNoTracking().AsQueryable();

            if (mealTypeId.HasValue)
                meals = meals.Where(m => m.MealTypeId == mealTypeId.Value);

            if (date.HasValue)
            {
                var ids = MealIdsForDate(date.Value);
                meals = meals.Where(m => ids.Contains(m.MealId));
            }

            var mealIds = meals.Select(m => m.MealId);

            var ratings = _context.RatingComment
                .AsNoTracking()
                .Where(rc => mealIds.Contains(rc.MealId));

            var totalMeals = meals.Count();
            var totalRatings = ratings.Count();
            var overallAvg = totalRatings == 0 ? 0m : ratings.Average(x => (decimal)x.Rating);

            return (totalMeals, totalRatings, overallAvg);
        }
        private IQueryable<int> MealIdsForDateRange(DateOnly dateFrom, DateOnly dateTo)
        {
            return (from md in _context.MenuDate.AsNoTracking()
                    join mm in _context.MenuMeal.AsNoTracking() on md.MenuId equals mm.MenuId
                    where md.Date >= dateFrom && md.Date <= dateTo
                    select mm.MealId).Distinct();
        }
        public (int TotalMeals, int TotalRatings, decimal OverallAverage, int MaxRating) GetOverallStats(DateOnly? dateFrom, DateOnly? dateTo)
        {
            var meals = _context.Meal.AsNoTracking().AsQueryable();

            if (dateFrom.HasValue || dateTo.HasValue)
            {
                var from = dateFrom ?? dateTo!.Value;
                var to = dateTo ?? dateFrom!.Value;

                var ids = MealIdsForDateRange(from, to);
                meals = meals.Where(m => ids.Contains(m.MealId));
            }

            var mealIds = meals.Select(m => m.MealId);

            var ratings = _context.RatingComment
                .AsNoTracking()
                .Where(rc => mealIds.Contains(rc.MealId));

            var totalMeals = meals.Count();
            var totalRatings = ratings.Count();
            var overallAvg = totalRatings == 0 ? 0m : ratings.Average(x => (decimal)x.Rating);
            var maxRating = totalRatings == 0 ? 0 : ratings.Max(x => x.Rating);

            return (totalMeals, totalRatings, overallAvg, maxRating);
        }
    }
}