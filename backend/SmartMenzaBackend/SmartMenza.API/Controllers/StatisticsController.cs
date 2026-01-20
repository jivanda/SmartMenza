using System;
using System.Globalization;
using System.Globalization;
using System.Globalization;
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

            if (!DateOnly.TryParseExact(date, "dd/MM/yyyy", CultureInfo.InvariantCulture, DateTimeStyles.None, out var d))
            {
                error = "Neispravan format datuma. Koristi dd-MM-yyyy.";
                return false;
            }

            parsed = d;
            return true;
        }

        private static bool TryParseDateRange(string? dateFrom, string? dateTo, out DateOnly? from, out DateOnly? to, out string? error)
        {
            from = null;
            to = null;
            error = null;

            if (!TryParseDate(dateFrom, out var f, out var errF))
            {
                error = errF;
                return false;
            }

            if (!TryParseDate(dateTo, out var t, out var errT))
            {
                error = errT;
                return false;
            }

            from = f;
            to = t;

            if (from != null && to != null && from > to)
            {
                error = "Parametar dateFrom ne može biti veći od dateTo.";
                return false;
            }

            return true;
        }

        [HttpGet("top-meals")]
        public IActionResult TopMeals(
            [FromHeader(Name = "UserId")] int userId,
            [FromQuery] string? dateFrom,
            [FromQuery] string? dateTo,
            [FromQuery] int? mealTypeId,
            [FromQuery] string sortBy = "count",
            [FromQuery] int limit = 10)
        {
            if (!IsEmployee(userId))
                return Unauthorized(new SimpleMessageDto { Message = "Samo zaposlenik menze može pristupiti statistici." });

            if (!TryParseDateRange(dateFrom, dateTo, out var dFrom, out var dTo, out var err))
                return BadRequest(new SimpleMessageDto { Message = err! });

            var result = _stats.GetTopMeals(dFrom, mealTypeId, sortBy, limit);
            return Ok(result);
        }

        [HttpGet("summary")]
        public IActionResult Summary(
            [FromHeader(Name = "UserId")] int userId,
            [FromQuery] string? dateFrom,
            [FromQuery] string? dateTo,
            [FromQuery] int? mealTypeId,
            [FromQuery] string sortBy = "count",
            [FromQuery] int limit = 10)
        {
            if (!IsEmployee(userId))
                return Unauthorized(new SimpleMessageDto { Message = "Samo zaposlenik menze može pristupiti statistici." });

            if (!TryParseDateRange(dateFrom, dateTo, out var dFrom, out var dTo, out var err))
                return BadRequest(new SimpleMessageDto { Message = err! });

            var result = _stats.GetSummary(dFrom, mealTypeId, sortBy, limit);
            return Ok(result);
        }
    }
}