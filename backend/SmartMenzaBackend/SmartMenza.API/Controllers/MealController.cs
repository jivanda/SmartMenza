using Microsoft.AspNetCore.Mvc;
using SmartMenza.Business.Services;
using SmartMenza.Domain.DTOs;
using SmartMenza.API.Helpers;

namespace SmartMenza.API.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class MealController : ControllerBase
    {
        private readonly MealService _mealService;

        public MealController(MealService mealService)
        {
            _mealService = mealService;
        }

        [HttpGet]
        public IActionResult GetAllMeals()
        {
            var meals = _mealService.GetAllMeals();
            foreach (var m in meals)
                m.ImageUrl = Request.ToAbsoluteImageUrl(m.ImageUrl);

            return Ok(meals);
        }

        [HttpGet("{mealId}")]
        public IActionResult GetMealById(int mealId)
        {
            var meal = _mealService.GetMealById(mealId);
            if (meal == null)
            {
                return NotFound(new SimpleMessageDto
                {
                    Message = "Jelo nije pronađeno."
                });
            }

            meal.ImageUrl = Request.ToAbsoluteImageUrl(meal.ImageUrl);
            return Ok(meal);
        }
    }
}