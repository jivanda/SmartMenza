using Microsoft.AspNetCore.Mvc;
using SmartMenza.Business.Services;
using SmartMenza.Domain.DTOs;
using Google.Apis.Auth;

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
                userId = korisnik.UserId,
                uloga = korisnik.Role?.RoleName
            });
        }

        [HttpPost("google")]
        public async Task<IActionResult> GoogleLogin([FromBody] GoogleLoginDto dto)
        {
            try
            {
                var payload = await GoogleJsonWebSignature.ValidateAsync(
                    dto.IdToken,
                    new GoogleJsonWebSignature.ValidationSettings
                    {
                        Audience = new[]
                        {
                    "35432297091-5kpm784irvq6n8hl2u3aiq10vj6b996l.apps.googleusercontent.com"
                        }
                    });

                var user = _userService.GetOrCreateGoogleUser(payload);

                return Ok(new
                {
                    poruka = "Google prijava uspješna!",
                    username = user.Username,
                    email = user.Email,
                    uloga = user.Role.RoleName,
                    userId = user.UserId
                });
            }
            catch
            {
                return Unauthorized("Google token nije valjan.");
            }
        }
    }
}