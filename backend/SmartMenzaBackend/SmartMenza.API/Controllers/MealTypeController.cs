using Microsoft.AspNetCore.Mvc;
using SmartMenza.Business.Services;
using SmartMenza.Domain.DTOs;

namespace SmartMenza.API.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class MealTypeController : ControllerBase
    {
        private readonly MealTypeService _mealTypeService;

        public MealTypeController(MealTypeService mealTypeService)
        {
            _mealTypeService = mealTypeService;
        }

        [HttpGet("name")]
        public IActionResult GetNameById(int mealTypeId)
        {
            var name = _mealTypeService.GetNameById(mealTypeId);
            if (name == null)
            {
                return NotFound(new SimpleMessageDto
                {
                    Message = "Tip jela nije pronađen."
                });
            }

            return Ok(name);
        }
    }
}