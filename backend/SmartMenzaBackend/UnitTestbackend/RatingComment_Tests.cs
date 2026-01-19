using System;
using System.Collections.Generic;
using System.Linq;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using SmartMenza.API.Controllers;
using SmartMenza.Business.Services;
using SmartMenza.Data.Context;
using SmartMenza.Data.Entities;
using SmartMenza.Data.Repositories.Implementations;
using SmartMenza.Domain.DTOs;
using Xunit;

namespace UnitTestbackend
{
    public class RatingCommentControllerTests
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
            var role = new Role { RoleId = 1, RoleName = "Student", Users = new List<UserAccount>() };
            var user = new UserAccount
            {
                UserId = 1,
                RoleId = 1,
                Username = "karlo",
                Email = "k@k.com",
                PasswordHash = "x",
                Role = role
            };

            var mealType = new MealType { MealTypeId = 1, Name = "Rucak", Meals = new List<Meal>() };
            var meal = new Meal
            {
                MealId = 1,
                Name = "Piletina",
                Price = 5,
                MealTypeId = 1,
                MealType = mealType
            };

            ctx.Role.Add(role);
            ctx.UserAccount.Add(user);
            ctx.MealType.Add(mealType);
            ctx.Meal.Add(meal);
            ctx.SaveChanges();
        }

        private static RatingCommentController CreateController(SmartMenzaContext ctx)
        {
            var repo = new RatingCommentRepository(ctx);
            var service = new RatingCommentService(repo);
            var userService = new UserService(ctx);
            return new RatingCommentController(service, userService);
        }

        [Fact]
        public void Create_ReturnsUnauthorized_WhenUserNotFound()
        {
            using var ctx = CreateContext();
            SeedBase(ctx);
            var controller = CreateController(ctx);

            var result = controller.Create(999, new RatingCommentCreateDto { MealId = 1, Rating = 5, Comment = "ok" });

            var obj = Assert.IsType<UnauthorizedObjectResult>(result);
            Assert.IsType<SimpleMessageDto>(obj.Value);
        }

        [Fact]
        public void Create_ReturnsBadRequest_WhenRatingInvalid()
        {
            using var ctx = CreateContext();
            SeedBase(ctx);
            var controller = CreateController(ctx);

            var result = controller.Create(1, new RatingCommentCreateDto { MealId = 1, Rating = 6, Comment = "x" });

            var bad = Assert.IsType<BadRequestObjectResult>(result);
            Assert.IsType<SimpleMessageDto>(bad.Value);
        }

        [Fact]
        public void Create_ReturnsBadRequest_WhenMealIdInvalid()
        {
            using var ctx = CreateContext();
            SeedBase(ctx);
            var controller = CreateController(ctx);

            var result = controller.Create(1, new RatingCommentCreateDto { MealId = 0, Rating = 5, Comment = "x" });

            var bad = Assert.IsType<BadRequestObjectResult>(result);
            Assert.IsType<SimpleMessageDto>(bad.Value);
        }

        [Fact]
        public void Create_ReturnsBadRequest_WhenDuplicate()
        {
            using var ctx = CreateContext();
            SeedBase(ctx);

            ctx.RatingComment.Add(new RatingComment
            {
                UserId = 1,
                MealId = 1,
                Rating = 4,
                Comment = "prije",
                Date = DateTime.UtcNow
            });
            ctx.SaveChanges();

            var controller = CreateController(ctx);

            var result = controller.Create(1, new RatingCommentCreateDto { MealId = 1, Rating = 5, Comment = "duplikat" });

            var bad = Assert.IsType<BadRequestObjectResult>(result);
            Assert.IsType<SimpleMessageDto>(bad.Value);
        }

        [Fact]
        public void Create_ReturnsOk_WhenValid()
        {
            using var ctx = CreateContext();
            SeedBase(ctx);
            var controller = CreateController(ctx);

            var result = controller.Create(1, new RatingCommentCreateDto { MealId = 1, Rating = 5, Comment = "super" });

            var ok = Assert.IsType<OkObjectResult>(result);
            Assert.IsType<SimpleMessageDto>(ok.Value);
            Assert.Equal(1, ctx.RatingComment.Count());
        }

        [Fact]
        public void Update_ReturnsUnauthorized_WhenUserNotFound()
        {
            using var ctx = CreateContext();
            SeedBase(ctx);
            var controller = CreateController(ctx);

            var result = controller.Update(1, 999, new RatingCommentUpdateDto { Rating = 3, Comment = "x" });

            var obj = Assert.IsAssignableFrom<ObjectResult>(result);
            Assert.Equal(401, obj.StatusCode);
            Assert.IsType<SimpleMessageDto>(obj.Value);
        }

        [Fact]
        public void Update_ReturnsBadRequest_WhenReviewNotFoundForUser()
        {
            using var ctx = CreateContext();
            SeedBase(ctx);
            var controller = CreateController(ctx);

            var result = controller.Update(1, 1, new RatingCommentUpdateDto { Rating = 3, Comment = "x" });

            var bad = Assert.IsType<BadRequestObjectResult>(result);
            Assert.IsType<SimpleMessageDto>(bad.Value);
        }

        [Fact]
        public void Update_ReturnsBadRequest_WhenRatingInvalid()
        {
            using var ctx = CreateContext();
            SeedBase(ctx);

            ctx.RatingComment.Add(new RatingComment
            {
                UserId = 1,
                MealId = 1,
                Rating = 4,
                Comment = "prije",
                Date = DateTime.UtcNow
            });
            ctx.SaveChanges();

            var controller = CreateController(ctx);

            var result = controller.Update(1, 1, new RatingCommentUpdateDto { Rating = 0, Comment = "x" });

            var bad = Assert.IsType<BadRequestObjectResult>(result);
            Assert.IsType<SimpleMessageDto>(bad.Value);
        }

        [Fact]
        public void Update_ReturnsOk_WhenValid()
        {
            using var ctx = CreateContext();
            SeedBase(ctx);

            ctx.RatingComment.Add(new RatingComment
            {
                UserId = 1,
                MealId = 1,
                Rating = 4,
                Comment = "prije",
                Date = DateTime.UtcNow
            });
            ctx.SaveChanges();

            var controller = CreateController(ctx);

            var result = controller.Update(1, 1, new RatingCommentUpdateDto { Rating = 2, Comment = "novo" });

            var ok = Assert.IsType<OkObjectResult>(result);
            Assert.IsType<SimpleMessageDto>(ok.Value);

            var updated = ctx.RatingComment.First(x => x.UserId == 1 && x.MealId == 1);
            Assert.Equal(2, updated.Rating);
            Assert.Equal("novo", updated.Comment);
        }

        [Fact]
        public void Delete_ReturnsBadRequest_WhenReviewNotFound()
        {
            using var ctx = CreateContext();
            SeedBase(ctx);
            var controller = CreateController(ctx);

            var result = controller.Delete(1, 1);

            var bad = Assert.IsType<BadRequestObjectResult>(result);
            Assert.IsType<SimpleMessageDto>(bad.Value);
        }

        [Fact]
        public void Delete_ReturnsOk_WhenValid()
        {
            using var ctx = CreateContext();
            SeedBase(ctx);

            ctx.RatingComment.Add(new RatingComment
            {
                UserId = 1,
                MealId = 1,
                Rating = 4,
                Comment = "prije",
                Date = DateTime.UtcNow
            });
            ctx.SaveChanges();

            var controller = CreateController(ctx);

            var result = controller.Delete(1, 1);

            var ok = Assert.IsType<OkObjectResult>(result);
            Assert.IsType<SimpleMessageDto>(ok.Value);
            Assert.Equal(0, ctx.RatingComment.Count());
        }

        [Fact]
        public void GetByMeal_ReturnsOk_WithDtos()
        {
            using var ctx = CreateContext();
            SeedBase(ctx);

            ctx.RatingComment.Add(new RatingComment
            {
                UserId = 1,
                MealId = 1,
                Rating = 5,
                Comment = "top",
                Date = DateTime.UtcNow
            });
            ctx.SaveChanges();

            var controller = CreateController(ctx);

            var result = controller.GetByMeal(1);

            var ok = Assert.IsType<OkObjectResult>(result);
            var list = Assert.IsAssignableFrom<IEnumerable<RatingCommentDto>>(ok.Value);
            var item = list.First();
            Assert.Equal(1, item.MealId);
            Assert.Equal(1, item.UserId);
            Assert.Equal(5, item.Rating);
            Assert.Equal("top", item.Comment);
            Assert.Equal("karlo", item.Username);
        }

        [Fact]
        public void GetSummary_ReturnsOk_WithCorrectAvgAndCount()
        {
            using var ctx = CreateContext();
            SeedBase(ctx);

            ctx.RatingComment.AddRange(
                new RatingComment { UserId = 1, MealId = 1, Rating = 5, Comment = "a", Date = DateTime.UtcNow },
                new RatingComment { UserId = 1, MealId = 1, Rating = 3, Comment = "b", Date = DateTime.UtcNow }
            );
            ctx.SaveChanges();

            var controller = CreateController(ctx);

            var result = controller.GetSummary(1);

            var ok = Assert.IsType<OkObjectResult>(result);
            var dto = Assert.IsType<RatingSummaryDto>(ok.Value);
            Assert.Equal(1, dto.MealId);
            Assert.Equal(2, dto.RatingsCount);
            Assert.Equal(4m, dto.AverageRating);
        }
    }
}