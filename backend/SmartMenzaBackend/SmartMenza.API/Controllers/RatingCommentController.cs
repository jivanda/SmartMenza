using Microsoft.AspNetCore.Mvc;
using SmartMenza.Business.Services;
using SmartMenza.Domain.DTOs;
using System.Linq;

namespace SmartMenza.API.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class RatingCommentController : ControllerBase
    {
        private readonly RatingCommentService _service;
        private readonly UserService _userService;

        public RatingCommentController(RatingCommentService service, UserService userService)
        {
            _service = service;
            _userService = userService;
        }

        [HttpPost]
        public IActionResult Create([FromHeader(Name = "UserId")] int userId, [FromBody] RatingCommentCreateDto dto)
        {
            if (_userService.GetUserById(userId) == null)
                return Unauthorized(new SimpleMessageDto { Message = "Korisnik nije pronađen." });

            var result = _service.Create(userId, dto);
            if (!result.Success)
                return BadRequest(new SimpleMessageDto { Message = result.ErrorMessage ?? "Greška." });

            return Ok(new SimpleMessageDto { Message = "Recenzija je spremljena." });
        }

        [HttpPut("meal/{mealId}")]
        public IActionResult Update(int mealId, [FromHeader(Name = "UserId")] int userId, [FromBody] RatingCommentUpdateDto dto)
        {
            if (_userService.GetUserById(userId) == null)
                return Unauthorized(new SimpleMessageDto { Message = "Korisnik nije pronađen." });

            var result = _service.Update(userId, mealId, dto);
            if (!result.Success)
                return BadRequest(new SimpleMessageDto { Message = result.ErrorMessage ?? "Greška." });

            return Ok(new SimpleMessageDto { Message = "Recenzija je ažurirana." });
        }

        [HttpDelete("meal/{mealId}")]
        public IActionResult Delete(int mealId, [FromHeader(Name = "UserId")] int userId)
        {
            if (_userService.GetUserById(userId) == null)
                return Unauthorized(new SimpleMessageDto { Message = "Korisnik nije pronađen." });

            var result = _service.Delete(userId, mealId);
            if (!result.Success)
                return BadRequest(new SimpleMessageDto { Message = result.ErrorMessage ?? "Greška." });

            return Ok(new SimpleMessageDto { Message = "Recenzija je obrisana." });
        }

        [HttpGet("meal/{mealId}")]
        public IActionResult GetByMeal(int mealId)
        {
            var list = _service.GetByMeal(mealId);

            var mapped = list.Select(r => new RatingCommentDto
            {
                MealId = r.MealId,
                UserId = r.UserId,
                Username = r.User?.Username ?? string.Empty,
                Rating = r.Rating,
                Comment = r.Comment
            });

            return Ok(mapped);
        }

        [HttpGet("meal/{mealId}/summary")]
        public IActionResult GetSummary(int mealId)
        {
            return Ok(_service.GetSummary(mealId));
        }

        [HttpGet("meal-stats")]
        public IActionResult GetMealStats()
        {
            var all = _service.GetAll();

            var stats = all
                .GroupBy(r => r.MealId)
                .Select(g => new MealRatingStatsDto
                {
                    MealId = g.Key,
                    NumberOfReviews = g.Count(),
                    AverageRating = g.Average(x => (decimal)x.Rating)
                })
                .ToList();

            return Ok(stats);
        }

        [HttpGet("meal/{mealId}/has")]
        public IActionResult HasReviewed(int mealId, [FromHeader(Name = "UserId")] int userId)
        {
            if (_userService.GetUserById(userId) == null)
                return Unauthorized(new SimpleMessageDto { Message = "Korisnik nije pronađen." });

            return Ok(_service.HasUserReviewedMeal(userId, mealId));
        }

        [HttpGet("meal/{mealId}/my")]
        public IActionResult GetMyReview(int mealId, [FromHeader(Name = "UserId")] int userId)
        {
            if (_userService.GetUserById(userId) == null)
                return Unauthorized(new SimpleMessageDto { Message = "Korisnik nije pronađen." });

            var rc = _service.GetByMeal(mealId).FirstOrDefault(x => x.UserId == userId);
            if (rc == null) return NotFound();

            return Ok(new RatingCommentDto
            {
                MealId = rc.MealId,
                UserId = rc.UserId,
                Username = rc.User?.Username ?? string.Empty,
                Rating = rc.Rating,
                Comment = rc.Comment
            });
        }
    }
}