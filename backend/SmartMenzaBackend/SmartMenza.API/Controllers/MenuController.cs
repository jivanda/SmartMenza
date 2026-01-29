using Microsoft.AspNetCore.Mvc;
using SmartMenza.Business.Services;
using SmartMenza.Domain.DTOs;
using System.Globalization;
using SmartMenza.API.Helpers;

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
                return BadRequest(new SimpleMessageDto
                {
                    Message = "Neispravan format datuma. Očekivan format je dd/MM/yyyy."
                });
            }

            var menus = _menuService.GetMenusByDate(DateOnly.FromDateTime(parsedDate));

            foreach (var menu in menus)
                foreach (var meal in menu.Meals)
                    meal.ImageUrl = Request.ToAbsoluteImageUrl(meal.ImageUrl);

            return Ok(menus);
        }

        [HttpGet("by-type")]
        public IActionResult GetAllByType([FromQuery] int menuTypeId)
        {
            if (menuTypeId <= 0)
            {
                return BadRequest(new { message = "Neispravan tip menija. Očekivan je pozitivan ID tipa menija." });
            }

            var menus = _menuService.GetMenusByType(menuTypeId);

            foreach (var menu in menus)
                foreach (var meal in menu.Meals)
                    meal.ImageUrl = Request.ToAbsoluteImageUrl(meal.ImageUrl);

            return Ok(menus);
        }

        [HttpGet("{menuId}")]
        public IActionResult GetMenuById(int menuId)
        {
            var menu = _menuService.GetMenuById(menuId);
            if (menu == null)
            {
                return NotFound(new SimpleMessageDto
                {
                    Message = "Meni s traženim ID-jem ne postoji."
                });
            }
            foreach (var meal in menu.Meals)
                meal.ImageUrl = Request.ToAbsoluteImageUrl(meal.ImageUrl);

            return Ok(menu);
        }

        [HttpPost("admin")]
        public IActionResult CreateMenu(
            [FromBody] CreateMenuDto dto,
            [FromHeader(Name = "Uloga")] string? roleHeader)
        {
            if (!string.Equals(roleHeader, "Employee", StringComparison.OrdinalIgnoreCase))
            {
                return StatusCode(StatusCodes.Status403Forbidden,
                    new SimpleMessageDto { Message = "Nemate ovlasti za kreiranje menija." });
            }

            var result = _menuService.CreateMenuForDate(dto);
            if (!result.Success)
            {
                return BadRequest(new SimpleMessageDto
                {
                    Message = result.ErrorMessage ?? "Greška pri kreiranju menija."
                });
            }

            return Ok(new CreateMenuResponseDto
            {
                Message = "Meni je uspješno spremljen.",
                MenuId = result.MenuId!.Value
            });
        }

        [HttpPost("admin/nodate")]
        public IActionResult CreateMenuNoDate(
            [FromBody] CreateMenuDtoNoDate dto,
            [FromHeader(Name = "Uloga")] string? roleHeader)
        {
            if (!string.Equals(roleHeader, "Employee", StringComparison.OrdinalIgnoreCase))
            {
                return StatusCode(StatusCodes.Status403Forbidden,
                    new SimpleMessageDto { Message = "Nemate ovlasti za kreiranje menija." });
            }

            var result = _menuService.CreateMenu(dto);
            if (!result.Success)
            {
                return BadRequest(new SimpleMessageDto
                {
                    Message = result.ErrorMessage ?? "Greška pri kreiranju menija."
                });
            }

            return Ok(new CreateMenuResponseDto
            {
                Message = "Meni je uspješno spremljen.",
                MenuId = result.MenuId!.Value
            });
        }

        [HttpPut("admin/{menuId}")]
        public IActionResult UpdateMenu(
            int menuId,
            [FromBody] UpdateMenuDto dto,
            [FromHeader(Name = "Uloga")] string? roleHeader)
        {
            if (!string.Equals(roleHeader, "Employee", StringComparison.OrdinalIgnoreCase))
            {
                return Unauthorized(new SimpleMessageDto
                {
                    Message = "Nedovoljna dopuštenja."
                });
            }

            var result = _menuService.UpdateMenu(menuId, dto);
            if (!result.Success)
            {
                return BadRequest(new SimpleMessageDto
                {
                    Message = result.ErrorMessage ?? "Greška pri ažuriranju menija."
                });
            }

            return Ok(new SimpleMessageDto
            {
                Message = "Meni je uspješno ažuriran."
            });
        }

        [HttpDelete("admin/{menuId}")]
        public IActionResult DeleteMenu(
            int menuId,
            [FromHeader(Name = "Uloga")] string? roleHeader)
        {
            if (!string.Equals(roleHeader, "Employee", StringComparison.OrdinalIgnoreCase))
            {
                return Unauthorized(new SimpleMessageDto
                {
                    Message = "Nedovoljna dopuštenja."
                });
            }

            var result = _menuService.DeleteMenu(menuId);
            if (!result.Success)
            {
                return BadRequest(new SimpleMessageDto
                {
                    Message = result.ErrorMessage ?? "Greška pri brisanju menija."
                });
            }

            return Ok(new SimpleMessageDto
            {
                Message = "Meni je uspješno obrisan."
            });
        }
    }
}