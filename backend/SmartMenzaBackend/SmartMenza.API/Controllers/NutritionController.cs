using System;
using System.Threading;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;
using SmartMenza.Business.Services;
using SmartMenza.Domain.DTOs;

namespace SmartMenza.API.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class NutritionController : ControllerBase
    {
        private readonly MealNutritionService _nutritionService;
        private readonly ILogger<NutritionController> _logger;

        public NutritionController(MealNutritionService nutritionService, ILogger<NutritionController> logger)
        {
            _nutritionService = nutritionService ?? throw new ArgumentNullException(nameof(nutritionService));
            _logger = logger ?? throw new ArgumentNullException(nameof(logger));
        }

        [HttpGet("analyze/menu/{menuId}")]
        [Produces("application/json")]
        [ProducesResponseType(typeof(NutritionResultDto), 200)]
        [ProducesResponseType(400)]
        [ProducesResponseType(500)]
        public async Task<IActionResult> AnalyzeMenu(int menuId, CancellationToken cancellationToken)
        {
            if (menuId <= 0)
            {
                _logger.LogWarning("AnalyzeMenu called with invalid menuId: {MenuId}.", menuId);
                return BadRequest(new { message = "Valid menuId is required." });
            }

            try
            {
                var result = await _nutritionService.AnalyzeMenuByNamesAsync(menuId, cancellationToken).ConfigureAwait(false);
                return Ok(result);
            }
            catch (InvalidOperationException ex)
            {
                _logger.LogWarning(ex, "Menu nutrition analysis failed for menu {MenuId}.", menuId);
                return BadRequest(new { message = ex.Message });
            }
            catch (OperationCanceledException)
            {
                _logger.LogWarning("Menu nutrition analysis cancelled by client for menu {MenuId}.", menuId);
                return StatusCode(499, new { message = "Request cancelled." });
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Unexpected error during menu nutrition analysis for menu {MenuId}.", menuId);
                return StatusCode(500, new { message = "Internal server error." });
            }
        }

        [HttpGet("assess/menu/{menuId}")]
        [Produces("application/json")]
        [ProducesResponseType(typeof(NutritionAssessmentDto), 200)]
        [ProducesResponseType(400)]
        [ProducesResponseType(500)]
        public async Task<IActionResult> AssessMenuHealth(int menuId, CancellationToken cancellationToken)
        {
            if (menuId <= 0)
            {
                _logger.LogWarning("AssessMenuHealth called with invalid menuId: {MenuId}.", menuId);
                return BadRequest(new { message = "Valid menuId is required." });
            }

            try
            {
                var dto = await _nutritionService.AssessMenuHealthAsync(menuId, cancellationToken).ConfigureAwait(false);
                return Ok(dto);
            }
            catch (InvalidOperationException ex)
            {
                _logger.LogWarning(ex, "Menu health assessment failed for menu {MenuId}.", menuId);
                return BadRequest(new { message = ex.Message });
            }
            catch (OperationCanceledException)
            {
                _logger.LogWarning("Menu health assessment cancelled by client for menu {MenuId}.", menuId);
                return StatusCode(499, new { message = "Request cancelled." });
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Unexpected error during menu health assessment for menu {MenuId}.", menuId);
                return StatusCode(500, new { message = "Internal server error." });
            }
        }
    }
}