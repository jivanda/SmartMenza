using Microsoft.AspNetCore.Mvc;
using SmartMenza.Business.Services;
using SmartMenza.Domain.DTOs;

namespace SmartMenza.API.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class MealReviewController : ControllerBase
    {
        private readonly MealReviewService _reviewService;
        private readonly UserService _userService;

        public MealReviewController(MealReviewService reviewService, UserService userService)
        {
            _reviewService = reviewService;
            _userService = userService;
        }

        [HttpPost]
        public IActionResult Create(
            [FromHeader(Name = "UserId")] int userId,
            [FromBody] MealReviewCreateDto dto)
        {
            if (_userService.GetUserById(userId) == null)
                return Unauthorized(new SimpleMessageDto { Message = "Korisnik nije pronađen." });

            var result = _reviewService.Create(userId, dto);
            if (!result.Success)
                return BadRequest(new SimpleMessageDto { Message = result.ErrorMessage ?? "Greška." });

            var r = result.Review!;

            return Ok(new MealReviewDto
            {
                MealReviewId = r.MealReviewId,
                MealId = r.MealId,
                UserId = r.UserId,
                Username = string.Empty,
                Rating = r.Rating,
                Comment = r.Comment,
                CreatedAt = r.CreatedAt,
                UpdatedAt = r.UpdatedAt
            });
        }

        [HttpPut("{reviewId}")]
        public IActionResult Update(
            int reviewId,
            [FromHeader(Name = "UserId")] int userId,
            [FromBody] MealReviewUpdateDto dto)
        {
            if (_userService.GetUserById(userId) == null)
                return Unauthorized(new SimpleMessageDto { Message = "Korisnik nije pronađen." });

            var result = _reviewService.Update(userId, reviewId, dto);
            if (!result.Success)
                return BadRequest(new SimpleMessageDto { Message = result.ErrorMessage ?? "Greška." });

            var r = result.Review!;

            return Ok(new MealReviewDto
            {
                MealReviewId = r.MealReviewId,
                MealId = r.MealId,
                UserId = r.UserId,
                Username = r.User?.Username ?? string.Empty,
                Rating = r.Rating,
                Comment = r.Comment,
                CreatedAt = r.CreatedAt,
                UpdatedAt = r.UpdatedAt
            });
        }

        [HttpDelete("{reviewId}")]
        public IActionResult Delete(
            int reviewId,
            [FromHeader(Name = "UserId")] int userId)
        {
            if (_userService.GetUserById(userId) == null)
                return Unauthorized(new SimpleMessageDto { Message = "Korisnik nije pronađen." });

            var result = _reviewService.Delete(userId, reviewId);
            if (!result.Success)
                return BadRequest(new SimpleMessageDto { Message = result.ErrorMessage ?? "Greška." });

            return Ok(new SimpleMessageDto { Message = "Recenzija je obrisana." });
        }

        [HttpGet("meal/{mealId}")]
        public IActionResult GetByMeal(int mealId)
        {
            var reviews = _reviewService.GetByMeal(mealId);

            var mapped = reviews.Select(r => new MealReviewDto
            {
                MealReviewId = r.MealReviewId,
                MealId = r.MealId,
                UserId = r.UserId,
                Username = r.User?.Username ?? string.Empty,
                Rating = r.Rating,
                Comment = r.Comment,
                CreatedAt = r.CreatedAt,
                UpdatedAt = r.UpdatedAt
            });

            return Ok(mapped);
        }
    }
}