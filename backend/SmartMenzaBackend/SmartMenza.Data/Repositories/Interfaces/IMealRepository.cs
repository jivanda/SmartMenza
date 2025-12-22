using SmartMenza.Data.Entities;
using System.Collections.Generic;

namespace SmartMenza.Data.Repositories.Interfaces
{
    public interface IMealRepository
    {
        List<Meal> GetAll();
        Meal? GetById(int mealId);
    }
}