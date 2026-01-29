using System.Collections.Generic;
using System.Linq;
using SmartMenza.Data.Entities;
using SmartMenza.Data.Repositories.Interfaces;
using SmartMenza.Domain.DTOs;

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
                    MealTypeId = m.MealTypeId,
                    Name = m.Name,
                    Description = m.Description,
                    Price = m.Price,
                    Calories = m.Calories,
                    Protein = m.Protein,
                    Carbohydrates = m.Carbohydrates,
                    Fat = m.Fat,
                    ImageUrl = m.ImageUrl,
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
                MealTypeId = m.MealTypeId,
                Name = m.Name,
                Description = m.Description,
                Price = m.Price,
                Calories = m.Calories,
                Protein = m.Protein,
                Carbohydrates = m.Carbohydrates,
                Fat = m.Fat,
                ImageUrl = m.ImageUrl,
            };
        }
    }
}