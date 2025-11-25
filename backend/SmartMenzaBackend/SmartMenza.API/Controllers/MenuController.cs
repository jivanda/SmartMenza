using Microsoft.AspNetCore.Mvc;
using SmartMenza.Business.Services;
using System.Globalization;

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
    }
}