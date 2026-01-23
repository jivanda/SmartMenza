using System;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;
using SmartMenza.Business.Services;

namespace SmartMenza.API.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class RecommendationController : ControllerBase
    {
        private readonly MealRecommendationService _recommendationService;
        private readonly ILogger<RecommendationController> _logger;

        public RecommendationController(MealRecommendationService recommendationService, ILogger<RecommendationController> logger)
        {
            _recommendationService = recommendationService;
            _logger = logger;
        }

        /// <summary>
        /// Recommend a single meal ID from the menu's meals.
        /// Returns 200 with the integer meal id (primitive) when successful.
        /// </summary>
        [HttpPost("menu/{menuId}")]
        public async Task<IActionResult> RecommendFromMenu(int menuId)
        {
            try
            {
                var chosenId = await _recommendationService.RecommendMealForMenuAsync(menuId);
                // Return primitive integer (JSON number). No explanation text.
                return Ok(chosenId);
            }
            catch (InvalidOperationException ex)
            {
                _logger.LogWarning(ex, "Recommendation failed for menu {MenuId}.", menuId);
                return BadRequest(new { message = ex.Message });
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Unexpected error while recommending meal for menu {MenuId}.", menuId);
                return StatusCode(500, new { message = "Internal server error." });
            }
        }
    }
}