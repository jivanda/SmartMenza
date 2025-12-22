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
            {
                return BadRequest(new ErrorResponseDto
                {
                    Message = "Korisnik s tom email adresom već postoji."
                });
            }

            return Ok(new AuthResponseDto
            {
                Message = "Registracija uspješna!",
                Username = dto.Username,
                Email = dto.Email,
                Role = dto.RoleName
            });
        }

        [HttpPost("login")]
        public IActionResult Login([FromBody] UserLoginDto dto)
        {
            var korisnik = _userService.LoginUser(dto);

            if (korisnik == null)
            {
                return Unauthorized(new ErrorResponseDto
                {
                    Message = "Pogrešan email ili lozinka."
                });
            }

            return Ok(new AuthResponseDto
            {
                Message = "Prijava uspješna!",
                UserId = korisnik.UserId,
                Username = korisnik.Username,
                Email = korisnik.Email,
                Role = korisnik.Role?.RoleName ?? string.Empty
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

                return Ok(new AuthResponseDto
                {
                    Message = "Google prijava uspješna!",
                    UserId = user.UserId,
                    Username = user.Username,
                    Email = user.Email,
                    Role = user.Role.RoleName
                });
            }
            catch
            {
                return Unauthorized(new ErrorResponseDto
                {
                    Message = "Google token nije valjan."
                });
            }
        }
    }
}