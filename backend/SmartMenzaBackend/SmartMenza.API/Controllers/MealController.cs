using Microsoft.AspNetCore.Mvc;
using SmartMenza.Business.Services;
using SmartMenza.Domain.DTOs;

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

            return Ok(meal);
        }
    }
}