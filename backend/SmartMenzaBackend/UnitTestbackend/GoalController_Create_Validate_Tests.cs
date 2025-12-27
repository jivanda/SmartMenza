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
    public class GoalController_Create_Validate_Tests
    {
        private static SmartMenzaContext CreateContext()
        {
            var options = new DbContextOptionsBuilder<SmartMenzaContext>()
                .UseInMemoryDatabase(Guid.NewGuid().ToString())
                .Options;

            return new SmartMenzaContext(options);
        }

        private static void SeedUser(SmartMenzaContext context, int userId, int roleId = 1, string roleName = "Student")
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

        [Fact]
        public void CreateGoal_ReturnsOk_WhenValid()
        {
            using var context = CreateContext();
            SeedUser(context, userId: 1);

            var controller = BuildController(context);

            var dto = new GoalCreateDto
            {
                Calories = 2000,
                TargetProteins = 150,
                TargetCarbs = 200,
                TargetFats = 60
            };

            var result = controller.CreateGoal(dto, userId: 1);

            var ok = Assert.IsType<OkObjectResult>(result);
            var goalDto = Assert.IsType<GoalDto>(ok.Value);

            Assert.True(goalDto.GoalId > 0);
            Assert.Equal(2000, goalDto.Calories);
            Assert.Equal(150, goalDto.Protein);
            Assert.Equal(200, goalDto.Carbohydrates);
            Assert.Equal(60, goalDto.Fat);
        }

        [Fact]
        public void CreateGoal_ReturnsBadRequest_WhenAnyValueIsZeroOrNegative()
        {
            using var context = CreateContext();
            SeedUser(context, userId: 1);

            var controller = BuildController(context);

            var dto = new GoalCreateDto
            {
                Calories = 0,
                TargetProteins = 100,
                TargetCarbs = 100,
                TargetFats = 50
            };

            var result = controller.CreateGoal(dto, userId: 1);

            var bad = Assert.IsType<BadRequestObjectResult>(result);
            var msg = Assert.IsType<SimpleMessageDto>(bad.Value);

            Assert.Contains("moraju biti veće od 0", msg.Message);
        }

        [Fact]
        public void CreateGoal_ReturnsBadRequest_WhenValuesAreUnrealisticallyLarge()
        {
            using var context = CreateContext();
            SeedUser(context, userId: 1);

            var controller = BuildController(context);

            var dto = new GoalCreateDto
            {
                Calories = 50001,
                TargetProteins = 10,
                TargetCarbs = 10,
                TargetFats = 10
            };

            var result = controller.CreateGoal(dto, userId: 1);

            var bad = Assert.IsType<BadRequestObjectResult>(result);
            var msg = Assert.IsType<SimpleMessageDto>(bad.Value);

            Assert.Contains("nerealno velike", msg.Message);
        }

        [Fact]
        public void CreateGoal_ReturnsBadRequest_WhenGoalAlreadyExistsForToday()
        {
            using var context = CreateContext();
            SeedUser(context, userId: 1);

            context.NutritionGoal.Add(new NutritionGoal
            {
                GoalId = 1,
                UserId = 1,
                Calories = 2000,
                Protein = 150,
                Carbohydrates = 200,
                Fat = 60,
                DateSet = DateOnly.FromDateTime(DateTime.UtcNow)
            });
            context.SaveChanges();

            var controller = BuildController(context);

            var dto = new GoalCreateDto
            {
                Calories = 2100,
                TargetProteins = 160,
                TargetCarbs = 210,
                TargetFats = 65
            };

            var result = controller.CreateGoal(dto, userId: 1);

            var bad = Assert.IsType<BadRequestObjectResult>(result);
            var msg = Assert.IsType<SimpleMessageDto>(bad.Value);

            Assert.Contains("već postavljen", msg.Message);
        }

        [Fact]
        public void ValidateGoal_ReturnsOk_WhenValid()
        {
            using var context = CreateContext();
            var controller = BuildController(context);

            var dto = new GoalValidationDto
            {
                TargetCalories = 2000,
                TargetProteins = 150,
                TargetCarbs = 200,
                TargetFats = 56
            };

            var result = controller.ValidateGoal(dto);

            var ok = Assert.IsType<OkObjectResult>(result);
            var msg = Assert.IsType<SimpleMessageDto>(ok.Value);

            Assert.Contains("valjan", msg.Message);
        }

        [Fact]
        public void ValidateGoal_ReturnsBadRequest_WhenNegative()
        {
            using var context = CreateContext();
            var controller = BuildController(context);

            var dto = new GoalValidationDto
            {
                TargetCalories = -1,
                TargetProteins = 1,
                TargetCarbs = 1,
                TargetFats = 1
            };

            var result = controller.ValidateGoal(dto);

            var bad = Assert.IsType<BadRequestObjectResult>(result);
            var msg = Assert.IsType<SimpleMessageDto>(bad.Value);

            Assert.Contains("veće od 0", msg.Message);
        }

        [Fact]
        public void ValidateGoal_ReturnsBadRequest_WhenMacrosNotInRealisticRelationToCalories()
        {
            using var context = CreateContext();
            var controller = BuildController(context);

            var dto = new GoalValidationDto
            {
                TargetCalories = 500,
                TargetProteins = 200,
                TargetCarbs = 200,
                TargetFats = 200
            };

            var result = controller.ValidateGoal(dto);

            var bad = Assert.IsType<BadRequestObjectResult>(result);
            var msg = Assert.IsType<SimpleMessageDto>(bad.Value);

            Assert.Contains("nisu u realnom odnosu", msg.Message);
        }
    }
}