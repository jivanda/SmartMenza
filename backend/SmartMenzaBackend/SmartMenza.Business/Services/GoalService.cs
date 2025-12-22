using SmartMenza.Data.Entities;
using SmartMenza.Data.Repositories.Interfaces;
using SmartMenza.Domain.DTOs;
using System;
using System.Collections.Generic;
using System.Linq;

namespace SmartMenza.Business.Services
{
    public class GoalService
    {
        private readonly IGoalRepository _goalRepository;

        public GoalService(IGoalRepository goalRepository)
        {
            _goalRepository = goalRepository;
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
                DateSet = DateOnly.FromDateTime(DateTime.UtcNow)
            };

            _goalRepository.Add(goal);
            _goalRepository.Save();

            return goal;
        }

        public NutritionGoal? GetGoalById(int goalId)
        {
            return _goalRepository.GetById(goalId);
        }

        public (bool Success, string? ErrorMessage, NutritionGoal? UpdatedGoal)
            UpdateGoal(int goalId, int userId, GoalUpdateDto dto)
        {
            var goal = _goalRepository.GetById(goalId);

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
            goal.DateSet = DateOnly.FromDateTime(DateTime.UtcNow);

            _goalRepository.Save();

            return (true, null, goal);
        }

        public (bool Success, string? ErrorMessage) DeleteGoal(int goalId, int userId)
        {
            var goal = _goalRepository.GetById(goalId);

            if (goal == null)
                return (false, "Cilj nije pronađen.");

            if (goal.UserId != userId)
                return (false, "Cilj ne pripada prijavljenom korisniku.");

            _goalRepository.Remove(goal);
            _goalRepository.Save();

            return (true, null);
        }

        public List<NutritionGoal> GetGoalsForUser(int userId)
        {
            return _goalRepository.GetByUser(userId);
        }

        public List<GoalSummaryDto> GetGoalSummariesForUser(int userId)
        {
            return _goalRepository
                .GetByUser(userId)
                .Select(g => new GoalSummaryDto
                {
                    Calories = g.Calories,
                    Protein = g.Protein,
                    Carbohydrates = g.Carbohydrates,
                    Fat = g.Fat
                })
                .ToList();
        }

        public List<NutritionGoal> GetGoalsByUser(int userId)
        {
            return _goalRepository.GetByUser(userId);
        }

        public bool UserHasGoalForToday(int userId)
        {
            var today = DateOnly.FromDateTime(DateTime.UtcNow);
            return _goalRepository.ExistsForToday(userId, today);
        }
    }
}