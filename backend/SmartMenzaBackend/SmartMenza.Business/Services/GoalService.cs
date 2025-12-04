using System;
using System.Linq;
using SmartMenza.Data.Context;
using SmartMenza.Domain.DTOs;
using SmartMenza.Domain.Entities;
using Microsoft.EntityFrameworkCore;

namespace SmartMenza.Business.Services
{
    public class GoalService
    {
        private readonly SmartMenzaContext _context;

        public GoalService(SmartMenzaContext context)
        {
            _context = context;
        }

        public NutritionGoal CreateGoal(GoalCreateDto dto, int userId)
        {
            var goal = new NutritionGoal
            {
                Calories = dto.Calories,
                Protein = dto.TargetProteins,
                Carbohydrates = dto.TargetCarbs,
                Fat = dto.TargetFats,
                UserId = userId,
                DateSet = DateTime.UtcNow
            };

            _context.NutritionGoal.Add(goal);
            _context.SaveChanges();

            return goal;
        }

        public NutritionGoal GetGoalById(int goalId)
        {
            return _context.NutritionGoal.FirstOrDefault(g => g.GoalId == goalId);
        }
    }
}