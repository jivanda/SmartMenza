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

        public NutritionGoal? GetGoalById(int goalId)
        {
            return _context.NutritionGoal.FirstOrDefault(g => g.GoalId == goalId);
        }

        public (bool Success, string? ErrorMessage, NutritionGoal? UpdatedGoal)
            UpdateGoal(int goalId, int userId, GoalUpdateDto dto)
        {
            var goal = _context.NutritionGoal.FirstOrDefault(g => g.GoalId == goalId);

            if (goal == null)
                return (false, "Cilj nije pronađen.", null);

            if (goal.UserId != userId)
                return (false, "Cilj ne pripada prijavljenom korisniku.", null);

            if (dto.Calories <= 0 || dto.TargetProteins <= 0)
                return (false, "Vrijednosti cilja moraju biti veće od 0.", null);

            goal.Calories = dto.Calories;
            goal.Protein = dto.TargetProteins;
            goal.Carbohydrates = dto.TargetCarbs;
            goal.Fat = dto.TargetFats;
            goal.DateSet = DateTime.UtcNow;

            _context.SaveChanges();

            return (true, null, goal);
        }

        public (bool Success, string? ErrorMessage)
            DeleteGoal(int goalId, int userId)
        {
            var goal = _context.NutritionGoal.FirstOrDefault(g => g.GoalId == goalId);

            if (goal == null)
                return (false, "Cilj nije pronađen.");

            if (goal.UserId != userId)
                return (false, "Cilj ne pripada prijavljenom korisniku.");

            _context.NutritionGoal.Remove(goal);
            _context.SaveChanges();

            return (true, null);
        }
    }
}