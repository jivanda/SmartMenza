using System;
using System.Globalization;
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

        [HttpPost("date/{date}")]
        public async Task<IActionResult> RecommendFromMenu(string date, [FromHeader(Name = "UserId")] int? userId)
        {
            try
            {
                if (string.IsNullOrWhiteSpace(date))
                {
                    _logger.LogWarning("No date provided for recommendation.");
                    return BadRequest(new { message = "Date is required in 'yyyy-MM-dd' format." });
                }

                if (!DateOnly.TryParseExact(date, "yyyy-MM-dd", CultureInfo.InvariantCulture, DateTimeStyles.None, out var parsedDate))
                {
                    _logger.LogWarning("Failed to parse date {Date} for recommendation.", date);
                    return BadRequest(new { message = "Invalid date format. Expected 'yyyy-MM-dd'." });
                }

                if (userId.HasValue && userId <= 0)
                {
                    _logger.LogWarning("Invalid UserId header provided: {UserId}", userId);
                    return BadRequest(new { message = "UserId header must be a positive integer." });
                }

                var chosenId = await _recommendationService.RecommendMealForDateAsync(parsedDate, userId, default);
                return Ok(chosenId);
            }
            catch (InvalidOperationException ex)
            {
                _logger.LogWarning(ex, "Recommendation failed for date {Date}.", date);
                return BadRequest(new { message = ex.Message });
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Unexpected error while recommending meal for date {Date}.", date);
                return StatusCode(500, new { message = "Internal server error." });
            }
        }
    }
}