using Microsoft.AspNetCore.Mvc;
using SmartMenza.Business.Services;
using SmartMenza.Domain.DTOs;

namespace SmartMenza.API.Controllers

{
    [ApiController]
    [Route("api/[controller]")]
    public class AuthController : ControllerBase
    {
        private readonly UserService _userService;

        public AuthController(UserService userService)
        {
            _userService = userService;
        }

        [HttpPost("registration")]
        public IActionResult Registration([FromBody] UserRegisterDto dto)
        {
            var uspjeh = _userService.RegisterUser(dto);

            if (!uspjeh)
                return BadRequest(new { poruka = "Korisnik s tom email adresom već postoji." });

            return Ok(new
            {
                poruka = "Registracija uspješna!",
                ime = dto.Username,
                email = dto.Email,
                uloga = dto.RoleName
            });
        }

        [HttpPost("login")]
        public IActionResult Login(UserLoginDto dto)
        {
            var korisnik = _userService.LoginUser(dto);
            if (korisnik == null)
                return Unauthorized("Pogrešan email ili lozinka.");

            return Ok(new
            {
                poruka = "Prijava uspješna!",
                korisnik.Username,
                korisnik.Email,
                uloga = korisnik.Role.RoleName
            });
        }
    }
}