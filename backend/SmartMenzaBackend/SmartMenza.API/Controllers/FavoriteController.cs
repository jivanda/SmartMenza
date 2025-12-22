using Microsoft.AspNetCore.Mvc;
using SmartMenza.Business.Services;
using SmartMenza.Domain.DTOs;

namespace SmartMenza.API.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class FavoriteController : ControllerBase
    {
        private readonly FavoriteService _favoriteService;
        private readonly UserService _userService;

        public FavoriteController(FavoriteService favoriteService, UserService userService)
        {
            _favoriteService = favoriteService;
            _userService = userService;
        }

        [HttpPost("add")]
        public IActionResult AddFavorite(
            [FromHeader(Name = "UserId")] int userId,
            [FromBody] FavoriteToggleDto dto)
        {
            if (_userService.GetUserById(userId) == null)
                return Unauthorized(new SimpleMessageDto { Message = "Korisnik nije pronađen." });

            bool added = _favoriteService.AddFavorite(userId, dto.MealId);

            if (!added)
                return BadRequest(new SimpleMessageDto { Message = "Jelo je već u favoritima." });

            return Ok(new SimpleMessageDto { Message = "Jelo dodano u favorite." });
        }

        [HttpDelete("remove")]
        public IActionResult RemoveFavorite(
            [FromHeader(Name = "UserId")] int userId,
            [FromBody] FavoriteToggleDto dto)
        {
            if (_userService.GetUserById(userId) == null)
                return Unauthorized(new SimpleMessageDto { Message = "Korisnik nije pronađen." });

            bool removed = _favoriteService.RemoveFavorite(userId, dto.MealId);

            if (!removed)
                return NotFound(new SimpleMessageDto { Message = "Jelo nije u favoritima." });

            return Ok(new SimpleMessageDto { Message = "Jelo uklonjeno iz favorita." });
        }

        [HttpGet("my")]
        public IActionResult GetMyFavorites([FromHeader(Name = "UserId")] int userId)
        {
            if (_userService.GetUserById(userId) == null)
                return Unauthorized(new SimpleMessageDto { Message = "Korisnik nije pronađen." });

            var favorites = _favoriteService.GetFavorites(userId);

            var mapped = favorites.Select(f => new FavoriteDto
            {
                MealId = f.MealId,
                MealName = f.Meal.Name,
                ImageUrl = f.Meal.ImageUrl,
                Calories = f.Meal.Calories,
                Protein = f.Meal.Protein
            });

            return Ok(mapped);
        }

        [HttpGet("status/{mealId}")]
        public IActionResult GetFavoriteStatus(
            int mealId,
            [FromHeader(Name = "UserId")] int userId)
        {
            if (_userService.GetUserById(userId) == null)
                return Unauthorized(new SimpleMessageDto { Message = "Korisnik nije pronađen." });

            bool isFav = _favoriteService.IsFavorite(userId, mealId);

            return Ok(new FavoriteStatusDto
            {
                MealId = mealId,
                IsFavorite = isFav
            });
        }
    }
}