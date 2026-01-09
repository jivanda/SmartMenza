using Microsoft.EntityFrameworkCore;
using SmartMenza.Data.Context;
using SmartMenza.Data.Entities;
using SmartMenza.Data.Repositories.Interfaces;
using System.Collections.Generic;
using System.Linq;

namespace SmartMenza.Data.Repositories.Implementations
{
    public class MealTypeRepository : IMealTypeRepository
    {
        private readonly SmartMenzaContext _context;

        public MealTypeRepository(SmartMenzaContext context)
        {
            _context = context;
        }

        public List<MealType> GetAll()
        {
            return _context.MealType
                .AsNoTracking()
                .OrderBy(m => m.Name)
                .ToList();
        }

        public MealType? GetById(int mealTypeId)
        {
            return _context.MealType
                .AsNoTracking()
                .FirstOrDefault(m => m.MealTypeId == mealTypeId);
        }
    }
}