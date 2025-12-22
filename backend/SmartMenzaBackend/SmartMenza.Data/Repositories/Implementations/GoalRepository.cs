using SmartMenza.Data.Context;
using SmartMenza.Data.Entities;
using SmartMenza.Data.Repositories.Interfaces;
using System.Collections.Generic;
using System.Linq;

namespace SmartMenza.Data.Repositories.Implementations
{
    public class GoalRepository : IGoalRepository
    {
        private readonly SmartMenzaContext _context;

        public GoalRepository(SmartMenzaContext context)
        {
            _context = context;
        }

        public NutritionGoal? GetById(int goalId)
            => _context.NutritionGoal.FirstOrDefault(g => g.GoalId == goalId);

        public List<NutritionGoal> GetByUser(int userId)
            => _context.NutritionGoal
                .Where(g => g.UserId == userId)
                .OrderByDescending(g => g.DateSet)
                .ToList();

        public bool ExistsForToday(int userId, DateOnly date)
            => _context.NutritionGoal.Any(g => g.UserId == userId && g.DateSet == date);

        public void Add(NutritionGoal goal)
            => _context.NutritionGoal.Add(goal);

        public void Remove(NutritionGoal goal)
            => _context.NutritionGoal.Remove(goal);

        public void Save()
            => _context.SaveChanges();
    }
}