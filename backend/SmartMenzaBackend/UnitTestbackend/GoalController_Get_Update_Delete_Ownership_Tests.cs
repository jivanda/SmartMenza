using System;
using System.Linq;
using Microsoft.AspNetCore.Http;
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
    public class GoalController_Get_Update_Delete_Ownership_Tests
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
                    PasswordHash = "x"
                });

            context.SaveChanges();
        }

        private static GoalController BuildController(SmartMenzaContext context)
        {
            var goalRepo = new GoalRepository(context);
            var goalService = new GoalService(goalRepo);
            var userService = new UserService(context);

            return new GoalController(goalService, userService);
        }

        private static void SetHeaderUserId(ControllerBase controller, int userId)
        {
            var http = new DefaultHttpContext();
            http.Request.Headers["UserId"] = userId.ToString();
            controller.ControllerContext = new ControllerContext { HttpContext = http };
        }

        [Fact]
        public void GetMyGoals_ReturnsBadRequest_WhenHeaderMissing()
        {
            using var context = CreateContext();
            SeedRoleAndUser(context, 1);

            var controller = BuildController(context);

            controller.ControllerContext = new ControllerContext { HttpContext = new DefaultHttpContext() };

            var result = controller.GetMyGoals();

            var bad = Assert.IsType<BadRequestObjectResult>(result);
            var msg = Assert.IsType<SimpleMessageDto>(bad.Value);

            Assert.Contains("UserId header je obavezan", msg.Message);
        }

        [Fact]
        public void GetMyGoals_ReturnsNotFound_WhenUserHasNoGoals()
        {
            using var context = CreateContext();
            SeedRoleAndUser(context, 1);

            var controller = BuildController(context);
            SetHeaderUserId(controller, 1);

            var result = controller.GetMyGoals();

            var notFound = Assert.IsType<NotFoundObjectResult>(result);
            var msg = Assert.IsType<SimpleMessageDto>(notFound.Value);

            Assert.Contains("nema spremljenih ciljeva", msg.Message);
        }

        [Fact]
        public void UpdateGoal_Succeeds_ForOwner()
        {
            using var context = CreateContext();
            SeedRoleAndUser(context, 1);

            context.NutritionGoal.Add(new NutritionGoal
            {
                GoalId = 10,
                UserId = 1,
                Calories = 2000,
                Protein = 150,
                Carbohydrates = 200,
                Fat = 60,
                DateSet = DateOnly.FromDateTime(DateTime.UtcNow.AddDays(-1))
            });
            context.SaveChanges();

            var controller = BuildController(context);

            var dto = new GoalUpdateDto
            {
                Calories = 2100,
                TargetProteins = 160,
                TargetCarbs = 210,
                TargetFats = 65
            };

            var result = controller.UpdateGoal(goalId: 10, dto: dto, userId: 1);

            var ok = Assert.IsType<OkObjectResult>(result);
            var updated = Assert.IsType<GoalDto>(ok.Value);

            Assert.Equal(10, updated.GoalId);
            Assert.Equal(2100, updated.Calories);
            Assert.Equal(160, updated.Protein);
            Assert.Equal(210, updated.Carbohydrates);
            Assert.Equal(65, updated.Fat);
        }

        [Fact]
        public void UpdateGoal_Fails_ForNonOwner()
        {
            using var context = CreateContext();
            SeedRoleAndUser(context, 1);
            SeedRoleAndUser(context, 2);

            context.NutritionGoal.Add(new NutritionGoal
            {
                GoalId = 11,
                UserId = 1,
                Calories = 2000,
                Protein = 150,
                Carbohydrates = 200,
                Fat = 60,
                DateSet = DateOnly.FromDateTime(DateTime.UtcNow.AddDays(-1))
            });
            context.SaveChanges();

            var controller = BuildController(context);

            var dto = new GoalUpdateDto
            {
                Calories = 2100,
                TargetProteins = 160,
                TargetCarbs = 210,
                TargetFats = 65
            };

            var result = controller.UpdateGoal(goalId: 11, dto: dto, userId: 2);

            var bad = Assert.IsType<BadRequestObjectResult>(result);
            var msg = Assert.IsType<SimpleMessageDto>(bad.Value);

            Assert.Contains("ne pripada", msg.Message);
        }

        [Fact]
        public void DeleteGoal_Succeeds_ForOwner()
        {
            using var context = CreateContext();
            SeedRoleAndUser(context, 1);

            context.NutritionGoal.Add(new NutritionGoal
            {
                GoalId = 12,
                UserId = 1,
                Calories = 2000,
                Protein = 150,
                Carbohydrates = 200,
                Fat = 60,
                DateSet = DateOnly.FromDateTime(DateTime.UtcNow.AddDays(-1))
            });
            context.SaveChanges();

            var controller = BuildController(context);

            var result = controller.DeleteGoal(goalId: 12, userId: 1);

            var ok = Assert.IsType<OkObjectResult>(result);
            var msg = Assert.IsType<SimpleMessageDto>(ok.Value);

            Assert.Contains("uspješno obrisan", msg.Message);
            Assert.False(context.NutritionGoal.Any(g => g.GoalId == 12));
        }

        [Fact]
        public void DeleteGoal_Fails_ForNonOwner()
        {
            using var context = CreateContext();
            SeedRoleAndUser(context, 1);
            SeedRoleAndUser(context, 2);

            context.NutritionGoal.Add(new NutritionGoal
            {
                GoalId = 13,
                UserId = 1,
                Calories = 2000,
                Protein = 150,
                Carbohydrates = 200,
                Fat = 60,
                DateSet = DateOnly.FromDateTime(DateTime.UtcNow.AddDays(-1))
            });
            context.SaveChanges();

            var controller = BuildController(context);

            var result = controller.DeleteGoal(goalId: 13, userId: 2);

            var bad = Assert.IsType<BadRequestObjectResult>(result);
            var msg = Assert.IsType<SimpleMessageDto>(bad.Value);

            Assert.Contains("ne pripada", msg.Message);
        }

        [Fact]
        public void CheckGoalOwnership_ReturnsOk_ForOwner()
        {
            using var context = CreateContext();
            SeedRoleAndUser(context, 1);

            context.NutritionGoal.Add(new NutritionGoal
            {
                GoalId = 14,
                UserId = 1,
                Calories = 2000,
                Protein = 150,
                Carbohydrates = 200,
                Fat = 60,
                DateSet = DateOnly.FromDateTime(DateTime.UtcNow)
            });
            context.SaveChanges();

            var controller = BuildController(context);

            var result = controller.CheckGoalOwnership(
                new GoalOwnershipCheckDto { GoalId = 14 },
                userId: 1);

            var ok = Assert.IsType<OkObjectResult>(result);
            var msg = Assert.IsType<SimpleMessageDto>(ok.Value);

            Assert.Contains("potvrđeno", msg.Message);
        }

        [Fact]
        public void CheckGoalOwnership_ReturnsUnauthorized_ForNonOwner()
        {
            using var context = CreateContext();
            SeedRoleAndUser(context, 1);
            SeedRoleAndUser(context, 2);

            context.NutritionGoal.Add(new NutritionGoal
            {
                GoalId = 15,
                UserId = 1,
                Calories = 2000,
                Protein = 150,
                Carbohydrates = 200,
                Fat = 60,
                DateSet = DateOnly.FromDateTime(DateTime.UtcNow)
            });
            context.SaveChanges();

            var controller = BuildController(context);

            var result = controller.CheckGoalOwnership(
                new GoalOwnershipCheckDto { GoalId = 15 },
                userId: 2);

            var unauthorized = Assert.IsType<UnauthorizedObjectResult>(result);
            var msg = Assert.IsType<SimpleMessageDto>(unauthorized.Value);

            Assert.Contains("ne pripada", msg.Message);
        }
    }
}