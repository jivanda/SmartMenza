using SmartMenza.Data.Repositories.Interfaces;
using SmartMenza.Domain.DTOs;
using System.Collections.Generic;
using System.Linq;

namespace SmartMenza.Business.Services
{
    public class MealService
    {
        private readonly IMealRepository _mealRepository;

        public MealService(IMealRepository mealRepository)
        {
            _mealRepository = mealRepository;
        }

        public List<MealDto> GetAllMeals()
        {
            return _mealRepository.GetAll()
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
            var m = _mealRepository.GetById(mealId);

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