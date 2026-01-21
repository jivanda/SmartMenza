using System;
using System.Linq;
using Microsoft.EntityFrameworkCore;
using SmartMenza.Data.Context;
using SmartMenza.Data.Entities;
using SmartMenza.Data.Repositories.Implementations;
using Xunit;

namespace UnitTestbackend
{
    public class StatisticsRepository_InMemoryTests
    {
        private static SmartMenzaContext CreateContext()
        {
            var options = new DbContextOptionsBuilder<SmartMenzaContext>()
                .UseInMemoryDatabase(Guid.NewGuid().ToString())
                .Options;
            return new SmartMenzaContext(options);
        }

        private static void SeedBase(SmartMenzaContext ctx)
        {
            if (!ctx.MealType.Any())
                ctx.MealType.Add(new MealType { MealTypeId = 1, Name = "Soup" });

            if (!ctx.Meal.Any())
                ctx.Meal.AddRange(
                    new Meal { MealId = 1, Name = "Chicken", MealTypeId = 1, Price = 10m },
                    new Meal { MealId = 2, Name = "Beef", MealTypeId = 1, Price = 12m }
                );

            if (!ctx.Menu.Any())
                ctx.Menu.Add(new Menu { MenuId = 1, Name = "Menu1" });

            if (!ctx.MenuMeal.Any())
                ctx.MenuMeal.Add(new MenuMeal { MenuId = 1, MealId = 1 });

            if (!ctx.MenuDate.Any())
                ctx.MenuDate.Add(new MenuDate { MenuId = 1, Date = DateOnly.FromDateTime(new DateTime(2025, 11, 3)) });

            ctx.SaveChanges();
        }

        [Fact]
        public void GetMealStats_ReturnsCountsAndAverages()
        {
            using var ctx = CreateContext();
            SeedBase(ctx);

            ctx.RatingComment.AddRange(
                new RatingComment { UserId = 1, MealId = 1, Rating = 5, Comment = "a", Date = DateTime.UtcNow },
                new RatingComment { UserId = 2, MealId = 1, Rating = 3, Comment = "b", Date = DateTime.UtcNow },
                new RatingComment { UserId = 3, MealId = 2, Rating = 4, Comment = "c", Date = DateTime.UtcNow }
            );
            ctx.SaveChanges();

            var repo = new StatisticsRepository(ctx);

            var statsAll = repo.GetMealStats(null, null);
            var m1 = statsAll.First(s => s.MealId == 1);
            var m2 = statsAll.First(s => s.MealId == 2);

            Assert.Equal(2, m1.RatingsCount);
            Assert.Equal(4.0m, m1.AverageRating);
            Assert.Equal(1, m2.RatingsCount);
            Assert.Equal(4.0m, m2.AverageRating);
        }

        [Fact]
        public void GetMealStats_FiltersByDateAndMealType()
        {
            using var ctx = CreateContext();
            SeedBase(ctx);

            ctx.RatingComment.AddRange(
                new RatingComment { UserId = 1, MealId = 1, Rating = 5, Comment = "a", Date = DateTime.UtcNow },
                new RatingComment { UserId = 2, MealId = 2, Rating = 2, Comment = "b", Date = DateTime.UtcNow }
            );
            ctx.SaveChanges();

            var repo = new StatisticsRepository(ctx);

            var statsOnDate = repo.GetMealStats(DateOnly.FromDateTime(new DateTime(2025, 11, 3)), null);
            Assert.Single(statsOnDate);
            Assert.Equal(1, statsOnDate[0].MealId);

            var statsByType = repo.GetMealStats(null, 1);
            Assert.Equal(2, statsByType.Count);
        }

        [Fact]
        public void GetSummary_ComputesTotalsAndAverage()
        {
            using var ctx = CreateContext();
            SeedBase(ctx);

            ctx.RatingComment.AddRange(
                new RatingComment { UserId = 1, MealId = 1, Rating = 5, Comment = "a", Date = DateTime.UtcNow },
                new RatingComment { UserId = 2, MealId = 1, Rating = 3, Comment = "b", Date = DateTime.UtcNow },
                new RatingComment { UserId = 3, MealId = 2, Rating = 4, Comment = "c", Date = DateTime.UtcNow }
            );
            ctx.SaveChanges();

            var repo = new StatisticsRepository(ctx);

            var (totalMeals, totalRatings, overallAvg) = repo.GetSummary(null, null);
            Assert.Equal(2, totalMeals);
            Assert.Equal(3, totalRatings);
            Assert.Equal(4.0m, overallAvg);

            var dateSummary = repo.GetSummary(DateOnly.FromDateTime(new DateTime(2025, 11, 3)), null);
            Assert.Equal(1, dateSummary.TotalMeals);
            Assert.Equal(2, dateSummary.TotalRatings);
            Assert.Equal(4.0m, dateSummary.OverallAverage);
        }
    }
}