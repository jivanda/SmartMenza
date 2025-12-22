using Microsoft.EntityFrameworkCore;
using SmartMenza.Data.Context;
using SmartMenza.Data.Entities;
using SmartMenza.Data.Repositories.Interfaces;
using System.Collections.Generic;
using System.Linq;

namespace SmartMenza.Data.Repositories.Implementations
{
    public class MealRepository : IMealRepository
    {
        private readonly SmartMenzaContext _context;

        public MealRepository(SmartMenzaContext context)
        {
            _context = context;
        }

        public List<Meal> GetAll()
        {
            return _context.Meal
                .AsNoTracking()
                .OrderBy(m => m.Name)
                .ToList();
        }

        public Meal? GetById(int mealId)
        {
            return _context.Meal
                .AsNoTracking()
                .FirstOrDefault(m => m.MealId == mealId);
        }
    }
}