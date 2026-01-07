using System;
using System.Linq;
using Microsoft.EntityFrameworkCore;
using Xunit;
using SmartMenza.Business.Services;
using SmartMenza.Data.Context;
using SmartMenza.Data.Entities;
using SmartMenza.Data.Repositories.Implementations;
using SmartMenza.Domain.DTOs;

namespace UnitTestbackend
{
    public class RatingCommentService_InMemoryTests
    {
        private static SmartMenzaContext CreateContext()
        {
            var options = new DbContextOptionsBuilder<SmartMenzaContext>()
                .UseInMemoryDatabase(Guid.NewGuid().ToString())
                .Options;

            return new SmartMenzaContext(options);
        }

        private static void SeedUserAndRole(SmartMenzaContext context, int userId = 1, int roleId = 1)
        {
            if (!context.Role.Any(r => r.RoleId == roleId))
            {
                context.Role.Add(new Role { RoleId = roleId, RoleName = "Student" });
                context.SaveChanges();
            }

            if (!context.UserAccount.Any(u => u.UserId == userId))
            {
                context.UserAccount.Add(new UserAccount
                {
                    UserId = userId,
                    RoleId = roleId,
                    Username = "tester",
                    Email = "tester@example.com",
                    PasswordHash = "x",
                    GoogleId = null
                });
                context.SaveChanges();
            }
        }

        private static void SeedMeal(SmartMenzaContext context, int mealId = 10, int mealTypeId = 1)
        {
            if (!context.MealType.Any(mt => mt.MealTypeId == mealTypeId))
            {
                context.MealType.Add(new MealType { MealTypeId = mealTypeId, Name = "Rucak" });
                context.SaveChanges();
            }

            if (!context.Meal.Any(m => m.MealId == mealId))
            {
                context.Meal.Add(new Meal
                {
                    MealId = mealId,
                    Name = "Jelo",
                    Price = 5,
                    MealTypeId = mealTypeId
                });
                context.SaveChanges();
            }
        }

        [Fact]
        public void Create_Success_WhenValid_AndNoDuplicate()
        {
            using var context = CreateContext();
            SeedUserAndRole(context, 1);
            SeedMeal(context, 10);

            var repo = new RatingCommentRepository(context);
            var svc = new RatingCommentService(repo);

            var result = svc.Create(1, new RatingCommentCreateDto
            {
                MealId = 10,
                Rating = 5,
                Comment = "Top"
            });

            Assert.True(result.Success);
            Assert.Null(result.ErrorMessage);
            Assert.True(context.RatingComment.Any(r => r.UserId == 1 && r.MealId == 10));
        }

        [Fact]
        public void Create_Fails_WhenMealNotFound()
        {
            using var context = CreateContext();
            SeedUserAndRole(context, 1);

            var repo = new RatingCommentRepository(context);
            var svc = new RatingCommentService(repo);

            var result = svc.Create(1, new RatingCommentCreateDto
            {
                MealId = 999,
                Rating = 5,
                Comment = "x"
            });

            Assert.False(result.Success);
            Assert.Equal("Jelo nije pronađeno.", result.ErrorMessage);
        }

        [Theory]
        [InlineData(0)]
        [InlineData(6)]
        public void Create_Fails_WhenRatingOutOfRange(int rating)
        {
            using var context = CreateContext();
            SeedUserAndRole(context, 1);
            SeedMeal(context, 10);

            var repo = new RatingCommentRepository(context);
            var svc = new RatingCommentService(repo);

            var result = svc.Create(1, new RatingCommentCreateDto
            {
                MealId = 10,
                Rating = rating,
                Comment = "x"
            });

            Assert.False(result.Success);
            Assert.Equal("Ocjena mora biti između 1 i 5.", result.ErrorMessage);
        }

        [Fact]
        public void Create_Fails_WhenDuplicateExists()
        {
            using var context = CreateContext();
            SeedUserAndRole(context, 1);
            SeedMeal(context, 10);

            context.RatingComment.Add(new RatingComment { UserId = 1, MealId = 10, Rating = 4, Comment = "ok" });
            context.SaveChanges();

            var repo = new RatingCommentRepository(context);
            var svc = new RatingCommentService(repo);

            var result = svc.Create(1, new RatingCommentCreateDto
            {
                MealId = 10,
                Rating = 5,
                Comment = "dup"
            });

            Assert.False(result.Success);
            Assert.Equal("Korisnik je već ocijenio ovo jelo.", result.ErrorMessage);
        }

        [Fact]
        public void Update_Success_UpdatesRatingAndComment()
        {
            using var context = CreateContext();
            SeedUserAndRole(context, 1);
            SeedMeal(context, 10);

            context.RatingComment.Add(new RatingComment { UserId = 1, MealId = 10, Rating = 2, Comment = "staro" });
            context.SaveChanges();

            var repo = new RatingCommentRepository(context);
            var svc = new RatingCommentService(repo);

            var result = svc.Update(1, 10, new RatingCommentUpdateDto
            {
                Rating = 5,
                Comment = "novo"
            });

            Assert.True(result.Success);
            Assert.Null(result.ErrorMessage);

            var updated = context.RatingComment.First(r => r.UserId == 1 && r.MealId == 10);
            Assert.Equal(5, updated.Rating);
            Assert.Equal("novo", updated.Comment);
        }

        [Fact]
        public void Update_Fails_WhenReviewNotFound()
        {
            using var context = CreateContext();
            SeedUserAndRole(context, 1);
            SeedMeal(context, 10);

            var repo = new RatingCommentRepository(context);
            var svc = new RatingCommentService(repo);

            var result = svc.Update(1, 10, new RatingCommentUpdateDto
            {
                Rating = 4,
                Comment = "x"
            });

            Assert.False(result.Success);
            Assert.Equal("Recenzija nije pronađena.", result.ErrorMessage);
        }

        [Theory]
        [InlineData(0)]
        [InlineData(6)]
        public void Update_Fails_WhenRatingOutOfRange(int rating)
        {
            using var context = CreateContext();
            SeedUserAndRole(context, 1);
            SeedMeal(context, 10);

            context.RatingComment.Add(new RatingComment { UserId = 1, MealId = 10, Rating = 3, Comment = "ok" });
            context.SaveChanges();

            var repo = new RatingCommentRepository(context);
            var svc = new RatingCommentService(repo);

            var result = svc.Update(1, 10, new RatingCommentUpdateDto
            {
                Rating = rating,
                Comment = "x"
            });

            Assert.False(result.Success);
            Assert.Equal("Ocjena mora biti između 1 i 5.", result.ErrorMessage);
        }

        [Fact]
        public void Delete_Success_RemovesReview()
        {
            using var context = CreateContext();
            SeedUserAndRole(context, 1);
            SeedMeal(context, 10);

            context.RatingComment.Add(new RatingComment { UserId = 1, MealId = 10, Rating = 4, Comment = "x" });
            context.SaveChanges();

            var repo = new RatingCommentRepository(context);
            var svc = new RatingCommentService(repo);

            var result = svc.Delete(1, 10);

            Assert.True(result.Success);
            Assert.Null(result.ErrorMessage);
            Assert.False(context.RatingComment.Any(r => r.UserId == 1 && r.MealId == 10));
        }

        [Fact]
        public void Delete_Fails_WhenReviewNotFound()
        {
            using var context = CreateContext();
            SeedUserAndRole(context, 1);
            SeedMeal(context, 10);

            var repo = new RatingCommentRepository(context);
            var svc = new RatingCommentService(repo);

            var result = svc.Delete(1, 10);

            Assert.False(result.Success);
            Assert.Equal("Recenzija nije pronađena.", result.ErrorMessage);
        }

        [Fact]
        public void GetByMeal_ReturnsOnlyMealReviews()
        {
            using var context = CreateContext();
            SeedUserAndRole(context, 1);
            SeedUserAndRole(context, 2);
            SeedMeal(context, 10);
            SeedMeal(context, 11);

            context.RatingComment.Add(new RatingComment { UserId = 1, MealId = 10, Rating = 5, Comment = "a" });
            context.RatingComment.Add(new RatingComment { UserId = 2, MealId = 10, Rating = 3, Comment = "b" });
            context.RatingComment.Add(new RatingComment { UserId = 1, MealId = 11, Rating = 1, Comment = "c" });
            context.SaveChanges();

            var repo = new RatingCommentRepository(context);
            var svc = new RatingCommentService(repo);

            var list = svc.GetByMeal(10);

            Assert.Equal(2, list.Count);
            Assert.All(list, x => Assert.Equal(10, x.MealId));
        }

        [Fact]
        public void GetSummary_ReturnsCountAndAverage()
        {
            using var context = CreateContext();
            SeedUserAndRole(context, 1);
            SeedUserAndRole(context, 2);
            SeedMeal(context, 10);

            context.RatingComment.Add(new RatingComment { UserId = 1, MealId = 10, Rating = 4, Comment = "a" });
            context.RatingComment.Add(new RatingComment { UserId = 2, MealId = 10, Rating = 2, Comment = "b" });
            context.SaveChanges();

            var repo = new RatingCommentRepository(context);
            var svc = new RatingCommentService(repo);

            var summary = svc.GetSummary(10);

            Assert.Equal(10, summary.MealId);
            Assert.Equal(2, summary.RatingsCount);
            Assert.Equal(3m, summary.AverageRating);
        }

        [Fact]
        public void GetSummary_ReturnsZero_WhenNoReviews()
        {
            using var context = CreateContext();
            SeedUserAndRole(context, 1);
            SeedMeal(context, 10);

            var repo = new RatingCommentRepository(context);
            var svc = new RatingCommentService(repo);

            var summary = svc.GetSummary(10);

            Assert.Equal(10, summary.MealId);
            Assert.Equal(0, summary.RatingsCount);
            Assert.Equal(0m, summary.AverageRating);
        }
    }
}