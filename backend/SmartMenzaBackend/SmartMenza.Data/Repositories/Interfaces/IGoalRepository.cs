using SmartMenza.Data.Entities;
using System.Collections.Generic;

namespace SmartMenza.Data.Repositories.Interfaces
{
    public interface IGoalRepository
    {
        NutritionGoal? GetById(int goalId);
        List<NutritionGoal> GetByUser(int userId);
        bool ExistsForToday(int userId, DateOnly date);
        void Add(NutritionGoal goal);
        void Remove(NutritionGoal goal);
        void Save();
    }
}