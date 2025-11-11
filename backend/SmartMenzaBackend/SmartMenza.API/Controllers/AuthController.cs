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

        [HttpPost("registracija")]
        public IActionResult Registracija([FromBody] UserRegisterDto dto)
        {
            var uspjeh = _userService.Registriraj(dto);

            if (!uspjeh)
                return BadRequest(new { poruka = "Korisnik s tom email adresom već postoji." });

            // vraćamo JSON objekt
            return Ok(new
            {
                poruka = "Registracija uspješna!",
                ime = dto.Ime,
                email = dto.Email,
                uloga = dto.Uloga
            });
        }

        [HttpPost("prijava")]
        public IActionResult Prijava(UserLoginDto dto)
        {
            var korisnik = _userService.Prijavi(dto);
            if (korisnik == null)
                return Unauthorized("Pogrešan email ili lozinka.");

            return Ok(new
            {
                poruka = "Prijava uspješna!",
                korisnik.Ime,
                korisnik.Email,
                korisnik.Uloga
            });
        }
    }
}