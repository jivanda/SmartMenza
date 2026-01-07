using System;
using Microsoft.AspNetCore.Mvc;
using SmartMenza.Business.Services;
using SmartMenza.Domain.DTOs;

namespace SmartMenza.API.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class StatisticsController : ControllerBase
    {
        private readonly StatisticsService _stats;
        private readonly UserService _users;

        public StatisticsController(StatisticsService stats, UserService users)
        {
            _stats = stats;
            _users = users;
        }

        private bool IsEmployee(int userId)
        {
            var u = _users.GetUserById(userId);
            return u != null && u.RoleId == 2;
        }

        private static bool TryParseDate(string? date, out DateOnly? parsed, out string? error)
        {
            parsed = null;
            error = null;

            if (string.IsNullOrWhiteSpace(date)) return true;

            if (!DateOnly.TryParse(date, out var d))
            {
                error = "Neispravan format datuma. Koristi yyyy-MM-dd.";
                return false;
            }

            parsed = d;
            return true;
        }

        [HttpGet("top-meals")]
        public IActionResult TopMeals(
            [FromHeader(Name = "UserId")] int userId,
            [FromQuery] string? date,
            [FromQuery] int? mealTypeId,
            [FromQuery] string sortBy = "count",
            [FromQuery] int limit = 10)
        {
            if (!IsEmployee(userId))
                return Unauthorized(new SimpleMessageDto { Message = "Samo zaposlenik menze može pristupiti statistici." });

            if (!TryParseDate(date, out var d, out var err))
                return BadRequest(new SimpleMessageDto { Message = err! });

            var result = _stats.GetTopMeals(d, mealTypeId, sortBy, limit);
            return Ok(result);
        }

        [HttpGet("summary")]
        public IActionResult Summary(
            [FromHeader(Name = "UserId")] int userId,
            [FromQuery] string? date,
            [FromQuery] int? mealTypeId,
            [FromQuery] string sortBy = "count",
            [FromQuery] int limit = 10)
        {
            if (!IsEmployee(userId))
                return Unauthorized(new SimpleMessageDto { Message = "Samo zaposlenik menze može pristupiti statistici." });

            if (!TryParseDate(date, out var d, out var err))
                return BadRequest(new SimpleMessageDto { Message = err! });

            var result = _stats.GetSummary(d, mealTypeId, sortBy, limit);
            return Ok(result);
        }
    }
}