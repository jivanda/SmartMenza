using System;
using System.Linq;
using System.Collections.Generic;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Xunit;
using SmartMenza.API.Controllers;
using SmartMenza.Business.Services;
using SmartMenza.Data.Context;
using SmartMenza.Data.Entities;
using SmartMenza.Data.Repositories.Implementations;
using SmartMenza.Domain.DTOs;

namespace UnitTestbackend
{
    public class FavoriteControllerTests
    {
        private static SmartMenzaContext CreateContext()
        {
            var options = new DbContextOptionsBuilder<SmartMenzaContext>()
                .UseInMemoryDatabase(Guid.NewGuid().ToString())
                .Options;

            return new SmartMenzaContext(options);
        }

        private static void SeedRoleAndUser(SmartMenzaContext context, int userId, int roleId = 1, string roleName = "Student")
        {
            if (!context.Role.Any(r => r.RoleId == roleId))
                context.Role.Add(new Role { RoleId = roleId, RoleName = roleName });

            if (!context.UserAccount.Any(u => u.UserId == userId))
                context.UserAccount.Add(new UserAccount
                {
                    UserId = userId,
                    RoleId = roleId,
                    Username = $"user{userId}",
                    Email = $"user{userId}@example.com",
                    PasswordHash = "x",
                    GoogleId = null,
                    Role = null!
                });

            context.SaveChanges();
        }

        private static void SeedMeal(SmartMenzaContext context, int mealId = 1, int mealTypeId = 1)
        {
            if (!context.MealType.Any(mt => mt.MealTypeId == mealTypeId))
                context.MealType.Add(new MealType { MealTypeId = mealTypeId, Name = "Glavno jelo", Meals = new List<Meal>() });

            if (!context.Meal.Any(m => m.MealId == mealId))
                context.Meal.Add(new Meal
                {
                    MealId = mealId,
                    Name = "Test jelo",
                    Description = "Opis",
                    Price = 5.50m,
                    MealTypeId = mealTypeId,
                    MealType = null!,
                    Calories = 500,
                    Protein = 30,
                    Carbohydrates = 60,
                    Fat = 10,
                    ImageUrl = "img",
                    MenuMeals = new List<MenuMeal>()
                });

            context.SaveChanges();
        }

        private static FavoriteController BuildController(SmartMenzaContext context)
        {
            var favRepo = new FavoriteRepository(context);
            var favService = new FavoriteService(favRepo);
            var userService = new UserService(context);
            return new FavoriteController(favService, userService);
        }

        [Fact]
        public void AddFavorite_ReturnsOk_WhenAdded()
        {
            using var context = CreateContext();
            SeedRoleAndUser(context, 1);
            SeedMeal(context, 1);

            var controller = BuildController(context);

            var result = controller.AddFavorite(1, new FavoriteToggleDto { MealId = 1 });

            var ok = Assert.IsType<OkObjectResult>(result);
            var msg = Assert.IsType<SimpleMessageDto>(ok.Value);
            Assert.Contains("dodano", msg.Message, StringComparison.OrdinalIgnoreCase);
            Assert.True(context.Favorite.Any(f => f.UserId == 1 && f.MealId == 1));
        }

        [Fact]
        public void AddFavorite_ReturnsBadRequest_WhenDuplicate()
        {
            using var context = CreateContext();
            SeedRoleAndUser(context, 1);
            SeedMeal(context, 1);

            context.Favorite.Add(new Favorite { UserId = 1, MealId = 1, User = null!, Meal = null! });
            context.SaveChanges();

            var controller = BuildController(context);

            var result = controller.AddFavorite(1, new FavoriteToggleDto { MealId = 1 });

            var bad = Assert.IsType<BadRequestObjectResult>(result);
            var msg = Assert.IsType<SimpleMessageDto>(bad.Value);
            Assert.Contains("već", msg.Message, StringComparison.OrdinalIgnoreCase);
        }

        [Fact]
        public void AddFavorite_ReturnsUnauthorized_WhenUserMissing()
        {
            using var context = CreateContext();
            SeedMeal(context, 1);

            var controller = BuildController(context);

            var result = controller.AddFavorite(999, new FavoriteToggleDto { MealId = 1 });

            var unauth = Assert.IsType<UnauthorizedObjectResult>(result);
            var msg = Assert.IsType<SimpleMessageDto>(unauth.Value);
            Assert.Contains("nije pronađen", msg.Message, StringComparison.OrdinalIgnoreCase);
        }

        [Fact]
        public void GetMyFavorites_ReturnsOk_WithMappedDtos()
        {
            using var context = CreateContext();
            SeedRoleAndUser(context, 1);
            SeedMeal(context, 1);

            context.Favorite.Add(new Favorite { UserId = 1, MealId = 1, User = null!, Meal = null! });
            context.SaveChanges();

            var controller = BuildController(context);

            var result = controller.GetMyFavorites(1);

            var ok = Assert.IsType<OkObjectResult>(result);
            var list = Assert.IsAssignableFrom<IEnumerable<FavoriteDto>>(ok.Value);

            var first = list.First();
            Assert.Equal(1, first.MealId);
            Assert.Equal("Test jelo", first.MealName);
            Assert.Equal("img", first.ImageUrl);
            Assert.Equal(500, first.Calories);
            Assert.Equal(30, first.Protein);
        }

        [Fact]
        public void GetMyFavorites_ReturnsUnauthorized_WhenUserMissing()
        {
            using var context = CreateContext();
            SeedMeal(context, 1);

            var controller = BuildController(context);

            var result = controller.GetMyFavorites(123);

            var unauth = Assert.IsType<UnauthorizedObjectResult>(result);
            var msg = Assert.IsType<SimpleMessageDto>(unauth.Value);
            Assert.Contains("nije pronađen", msg.Message, StringComparison.OrdinalIgnoreCase);
        }

        [Fact]
        public void RemoveFavorite_ReturnsOk_WhenExists()
        {
            using var context = CreateContext();
            SeedRoleAndUser(context, 1);
            SeedMeal(context, 1);

            context.Favorite.Add(new Favorite { UserId = 1, MealId = 1, User = null!, Meal = null! });
            context.SaveChanges();

            var controller = BuildController(context);

            var result = controller.RemoveFavorite(1, new FavoriteToggleDto { MealId = 1 });

            var ok = Assert.IsType<OkObjectResult>(result);
            var msg = Assert.IsType<SimpleMessageDto>(ok.Value);
            Assert.Contains("uklonj", msg.Message, StringComparison.OrdinalIgnoreCase);
            Assert.False(context.Favorite.Any(f => f.UserId == 1 && f.MealId == 1));
        }

        [Fact]
        public void RemoveFavorite_ReturnsNotFound_WhenNotInFavorites()
        {
            using var context = CreateContext();
            SeedRoleAndUser(context, 1);
            SeedMeal(context, 1);

            var controller = BuildController(context);

            var result = controller.RemoveFavorite(1, new FavoriteToggleDto { MealId = 1 });

            var notFound = Assert.IsType<NotFoundObjectResult>(result);
            var msg = Assert.IsType<SimpleMessageDto>(notFound.Value);
            Assert.Contains("nije", msg.Message, StringComparison.OrdinalIgnoreCase);
        }

        [Fact]
        public void RemoveFavorite_ReturnsUnauthorized_WhenUserMissing()
        {
            using var context = CreateContext();
            SeedMeal(context, 1);

            var controller = BuildController(context);

            var result = controller.RemoveFavorite(999, new FavoriteToggleDto { MealId = 1 });

            var unauth = Assert.IsType<UnauthorizedObjectResult>(result);
            var msg = Assert.IsType<SimpleMessageDto>(unauth.Value);
            Assert.Contains("nije pronađen", msg.Message, StringComparison.OrdinalIgnoreCase);
        }

        [Fact]
        public void GetFavoriteStatus_ReturnsOk_True_WhenExists()
        {
            using var context = CreateContext();
            SeedRoleAndUser(context, 1);
            SeedMeal(context, 1);

            context.Favorite.Add(new Favorite { UserId = 1, MealId = 1, User = null!, Meal = null! });
            context.SaveChanges();

            var controller = BuildController(context);

            var result = controller.GetFavoriteStatus(1, 1);

            var ok = Assert.IsType<OkObjectResult>(result);
            var dto = Assert.IsType<FavoriteStatusDto>(ok.Value);
            Assert.Equal(1, dto.MealId);
            Assert.True(dto.IsFavorite);
        }

        [Fact]
        public void GetFavoriteStatus_ReturnsOk_False_WhenNotExists()
        {
            using var context = CreateContext();
            SeedRoleAndUser(context, 1);
            SeedMeal(context, 1);

            var controller = BuildController(context);

            var result = controller.GetFavoriteStatus(1, 1);

            var ok = Assert.IsType<OkObjectResult>(result);
            var dto = Assert.IsType<FavoriteStatusDto>(ok.Value);
            Assert.Equal(1, dto.MealId);
            Assert.False(dto.IsFavorite);
        }

        [Fact]
        public void GetFavoriteStatus_ReturnsUnauthorized_WhenUserMissing()
        {
            using var context = CreateContext();
            SeedMeal(context, 1);

            var controller = BuildController(context);

            var result = controller.GetFavoriteStatus(1, 999);

            var unauth = Assert.IsType<UnauthorizedObjectResult>(result);
            var msg = Assert.IsType<SimpleMessageDto>(unauth.Value);
            Assert.Contains("nije pronađen", msg.Message, StringComparison.OrdinalIgnoreCase);
        }
    }
}