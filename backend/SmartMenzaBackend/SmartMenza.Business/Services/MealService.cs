using Microsoft.EntityFrameworkCore;
using SmartMenza.Data.Context;
using SmartMenza.Domain.DTOs;
using SmartMenza.Domain.Entities;
using System.Collections.Generic;
using System.Linq;

namespace SmartMenza.Business.Services
{
    public class MealService
    {
        private readonly SmartMenzaContext _context;

        public MealService(SmartMenzaContext context)
        {
            _context = context;
        }

        public List<MealDto> GetAllMeals()
        {
            return _context.Meal
                .AsNoTracking()
                .OrderBy(m => m.Name)
                .Select(m => new MealDto
                {
                    MealId = m.MealId,
                    Name = m.Name,
                    Description = m.Description,
                    Price = m.Price,
                    Calories = m.Calories,
                    Protein = m.Protein,
                    Carbohydrates = m.Carbohydrates,
                    Fat = m.Fat
                })
                .ToList();
        }

        public MealDto? GetMealById(int mealId)
        {
            var m = _context.Meal
                .AsNoTracking()
                .FirstOrDefault(x => x.MealId == mealId);

            if (m == null)
                return null;

            return new MealDto
            {
                MealId = m.MealId,
                Name = m.Name,
                Description = m.Description,
                Price = m.Price,
                Calories = m.Calories,
                Protein = m.Protein,
                Carbohydrates = m.Carbohydrates,
                Fat = m.Fat
            };
        }
    }
}