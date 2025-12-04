using Microsoft.AspNetCore.Mvc;
using SmartMenza.Business.Services;
using SmartMenza.Domain.DTOs;
using System.Globalization;
using System.Linq;

namespace SmartMenza.API.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class MenuController : ControllerBase
    {
        private readonly MenuService _menuService;

        public MenuController(MenuService menuService)
        {
            _menuService = menuService;
        }

        [HttpGet]
        public IActionResult GetByDate([FromQuery] string date)
        {
            if (!DateTime.TryParseExact(
                    date,
                    "dd/MM/yyyy",
                    CultureInfo.InvariantCulture,
                    DateTimeStyles.None,
                    out var parsedDate))
            {
                return BadRequest(new
                {
                    message = "Neispravan format datuma. Očekivan format je dd/MM/yyyy (npr. 03/11/2025)."
                });
            }

            var menu = _menuService.GetMenuByDate(parsedDate);

            if (menu == null)
                return NotFound(new { message = "Meni za traženi datum ne postoji." });

            return Ok(menu);
        }

        // NEW: returns all menus for the given date (same input as GetByDate)
        [HttpGet("all")]
        public IActionResult GetAllByDate([FromQuery] string date)
        {
            if (!DateTime.TryParseExact(
                    date,
                    "dd/MM/yyyy",
                    CultureInfo.InvariantCulture,
                    DateTimeStyles.None,
                    out var parsedDate))
            {
                return BadRequest(new
                {
                    message = "Neispravan format datuma. Očekivan format je dd/MM/yyyy (npr. 03/11/2025)."
                });
            }

            var menus = _menuService.GetMenusByDate(parsedDate);

            if (menus == null || !menus.Any())
                return NotFound(new { message = "Nema menija za traženi datum." });

            return Ok(menus);
        }

        [HttpPost("admin")]
        public IActionResult CreateMenu(
            [FromBody] CreateMenuDto dto,
            [FromHeader(Name = "Uloga")] string? roleHeader)
        {
            if (!string.Equals(roleHeader, "Employee", StringComparison.OrdinalIgnoreCase))
            {
                return StatusCode(StatusCodes.Status403Forbidden, new
                {
                    message = "Nemate ovlasti za kreiranje menija."
                });
            }

            var result = _menuService.CreateMenuForDate(dto);

            if (!result.Success)
            {
                return BadRequest(new
                {
                    message = result.ErrorMessage
                });
            }

            return Ok(new
            {
                message = "Meni je uspješno spremljen.",
                menuId = result.MenuId
            });
        }

        [HttpPut("admin/{menuId}")]
        public IActionResult UpdateMenu(
            int menuId,
            [FromBody] UpdateMenuDto dto,
            [FromHeader(Name = "Uloga")] string? roleHeader)
        {
            if (!string.Equals(roleHeader, "Employee", StringComparison.OrdinalIgnoreCase))
                return Unauthorized("Nedovoljna dopuštenja");

            var result = _menuService.UpdateMenu(menuId, dto);
            if (!result.Success)
                return BadRequest(new { message = result.ErrorMessage });

            return Ok(new { message = "Meni je uspješno ažuriran" });
        }

        [HttpDelete("admin/{menuId}")]
        public IActionResult DeleteMenu(
            int menuId,
            [FromHeader(Name = "Uloga")] string? roleHeader)
        {
            if (!string.Equals(roleHeader, "Employee", StringComparison.OrdinalIgnoreCase))
                return Unauthorized("Nedovoljna dopuštenja");

            var result = _menuService.DeleteMenu(menuId);
            if (!result.Success)
                return BadRequest(new { message = result.ErrorMessage });

            return Ok(new { message = "Meni je uspješno obrisan" });
        }

    }
}