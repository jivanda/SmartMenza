using System;
using System.Globalization;
using System.Threading;
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
        [Produces("application/json")]
        public async Task<IActionResult> RecommendFromMenu(string date, [FromHeader(Name = "UserId")] int? userId, CancellationToken cancellationToken = default)
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

                if (!userId.HasValue)
                {
                    _logger.LogWarning("UserId header missing for recommendation request.");
                    return BadRequest(new { message = "UserId header is required and must be a positive integer." });
                }

                if (userId <= 0)
                {
                    _logger.LogWarning("Invalid UserId header provided: {UserId}", userId);
                    return BadRequest(new { message = "UserId header must be a positive integer." });
                }

                var chosenId = await _recommendationService.RecommendMealForDateAsync(parsedDate, userId.Value, cancellationToken).ConfigureAwait(false);
                return Ok(chosenId);
            }
            catch (InvalidOperationException ex)
            {
                _logger.LogWarning(ex, "Recommendation failed for date {Date}.", date);
                return BadRequest(new { message = ex.Message });
            }
            catch (OperationCanceledException)
            {
                _logger.LogWarning("Recommendation cancelled by client for date {Date}.", date);
                return StatusCode(499, new { message = "Request cancelled." });
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Unexpected error while recommending meal for date {Date}.", date);
                return StatusCode(500, new { message = "Internal server error." });
            }
        }
    }
}