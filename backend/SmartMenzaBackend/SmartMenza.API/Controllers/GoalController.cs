using Microsoft.AspNetCore.Mvc;
using SmartMenza.Business.Services;
using SmartMenza.Domain.DTOs;
using System.Linq;

namespace SmartMenza.API.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class GoalController : ControllerBase
    {
        private readonly GoalService _goalService;
        private readonly UserService _userService;

        public GoalController(GoalService goalService, UserService userService)
        {
            _goalService = goalService;
            _userService = userService;
        }

        [HttpPost("create")]
        public IActionResult CreateGoal(
            [FromBody] GoalCreateDto goalDto,
            [FromHeader(Name = "UserId")] int userId)
        {
            var user = _userService.GetUserById(userId);
            if (user == null)
            {
                return Unauthorized(new SimpleMessageDto
                {
                    Message = "Korisnik nije pronađen."
                });
            }

            if (goalDto.Calories <= 0 ||
                goalDto.TargetProteins <= 0 ||
                goalDto.TargetCarbs <= 0 ||
                goalDto.TargetFats <= 0)
            {
                return BadRequest(new SimpleMessageDto
                {
                    Message = "Sve vrijednosti cilja moraju biti veće od 0."
                });
            }

            if (goalDto.Calories > 50000 ||
                goalDto.TargetProteins > 50000 ||
                goalDto.TargetCarbs > 50000 ||
                goalDto.TargetFats > 50000)
            {
                return BadRequest(new SimpleMessageDto
                {
                    Message = "Vrijednosti ciljeva su nerealno velike."
                });
            }

            if (_goalService.UserHasGoalForToday(userId))
            {
                return BadRequest(new SimpleMessageDto
                {
                    Message = "Cilj za današnji dan je već postavljen."
                });
            }

            var created = _goalService.CreateGoal(goalDto, userId);

            return Ok(new GoalDto
            {
                GoalId = created.GoalId,
                Calories = created.Calories,
                Protein = created.Protein,
                Carbohydrates = created.Carbohydrates,
                Fat = created.Fat,
                DateSet = created.DateSet
            });
        }

        [HttpPost("validate")]
        public IActionResult ValidateGoal([FromBody] GoalValidationDto goalDto)
        {
            if (goalDto == null)
            {
                return BadRequest(new SimpleMessageDto
                {
                    Message = "Podaci nisu poslani."
                });
            }

            if (goalDto.TargetCalories <= 0 ||
                goalDto.TargetProteins <= 0 ||
                goalDto.TargetCarbs <= 0 ||
                goalDto.TargetFats <= 0)
            {
                return BadRequest(new SimpleMessageDto
                {
                    Message = "Sve vrijednosti moraju biti veće od 0."
                });
            }

            if (goalDto.TargetCalories > 50000 ||
                goalDto.TargetProteins > 50000 ||
                goalDto.TargetCarbs > 50000 ||
                goalDto.TargetFats > 50000)
            {
                return BadRequest(new SimpleMessageDto
                {
                    Message = "Vrijednosti ciljeva su nerealno velike."
                });
            }

            var calculatedCalories =
                goalDto.TargetProteins * 4 +
                goalDto.TargetCarbs * 4 +
                goalDto.TargetFats * 9;

            if (calculatedCalories > goalDto.TargetCalories * 1.2m ||
                calculatedCalories < goalDto.TargetCalories * 0.8m)
            {
                return BadRequest(new SimpleMessageDto
                {
                    Message = "Makronutrijenti nisu u realnom odnosu s kalorijama."
                });
            }

            return Ok(new SimpleMessageDto
            {
                Message = "Cilj je valjan."
            });
        }

        [HttpPost("check-user")]
        public IActionResult CheckGoalOwnership(
            [FromBody] GoalOwnershipCheckDto checkDto,
            [FromHeader(Name = "UserId")] int userId)
        {
            var user = _userService.GetUserById(userId);
            if (user == null)
            {
                return Unauthorized(new SimpleMessageDto
                {
                    Message = "Korisnik nije pronađen."
                });
            }

            var goal = _goalService.GetGoalById(checkDto.GoalId);
            if (goal == null || goal.UserId != userId)
            {
                return Unauthorized(new SimpleMessageDto
                {
                    Message = "Ovaj cilj ne pripada trenutno prijavljenom korisniku."
                });
            }

            return Ok(new SimpleMessageDto
            {
                Message = "Vlasništvo cilja je uspješno potvrđeno."
            });
        }

        [HttpPut("{goalId}")]
        public IActionResult UpdateGoal(
            int goalId,
            [FromBody] GoalUpdateDto dto,
            [FromHeader(Name = "UserId")] int userId)
        {
            var user = _userService.GetUserById(userId);
            if (user == null)
            {
                return Unauthorized(new SimpleMessageDto
                {
                    Message = "Korisnik nije pronađen."
                });
            }

            if (dto.Calories <= 0 ||
                dto.TargetProteins <= 0 ||
                dto.TargetCarbs <= 0 ||
                dto.TargetFats <= 0)
            {
                return BadRequest(new SimpleMessageDto
                {
                    Message = "Sve vrijednosti cilja moraju biti veće od 0."
                });
            }

            if (dto.Calories > 50000 ||
                dto.TargetProteins > 50000 ||
                dto.TargetCarbs > 50000 ||
                dto.TargetFats > 50000)
            {
                return BadRequest(new SimpleMessageDto
                {
                    Message = "Vrijednosti ciljeva su nerealno velike."
                });
            }

            var result = _goalService.UpdateGoal(goalId, userId, dto);
            if (!result.Success)
            {
                return BadRequest(new SimpleMessageDto
                {
                    Message = result.ErrorMessage ?? "Greška pri ažuriranju cilja."
                });
            }

            var updated = result.UpdatedGoal!;

            return Ok(new GoalDto
            {
                GoalId = updated.GoalId,
                Calories = updated.Calories,
                Protein = updated.Protein,
                Carbohydrates = updated.Carbohydrates,
                Fat = updated.Fat,
                DateSet = updated.DateSet
            });
        }

        [HttpDelete("{goalId}")]
        public IActionResult DeleteGoal(
            int goalId,
            [FromHeader(Name = "UserId")] int userId)
        {
            var user = _userService.GetUserById(userId);
            if (user == null)
            {
                return Unauthorized(new SimpleMessageDto
                {
                    Message = "Korisnik nije pronađen."
                });
            }

            var result = _goalService.DeleteGoal(goalId, userId);
            if (!result.Success)
            {
                return BadRequest(new SimpleMessageDto
                {
                    Message = result.ErrorMessage ?? "Greška pri brisanju cilja."
                });
            }

            return Ok(new SimpleMessageDto
            {
                Message = "Cilj je uspješno obrisan."
            });
        }

        [HttpGet("myGoal")]
        public IActionResult GetMyGoals()
        {
            if (!Request.Headers.TryGetValue("UserId", out var userIdHeader) ||
                !int.TryParse(userIdHeader, out var userId) ||
                userId <= 0)
            {
                return BadRequest(new SimpleMessageDto
                {
                    Message = "UserId header je obavezan."
                });
            }

            var user = _userService.GetUserById(userId);
            if (user == null)
            {
                return NotFound(new SimpleMessageDto
                {
                    Message = "Korisnik nije pronađen."
                });
            }

            var goals = _goalService.GetGoalsByUser(userId);
            if (!goals.Any())
            {
                return NotFound(new SimpleMessageDto
                {
                    Message = "Korisnik nema spremljenih ciljeva."
                });
            }

            return Ok(goals.Select(g => new GoalDto
            {
                GoalId = g.GoalId,
                Calories = g.Calories,
                Protein = g.Protein,
                Carbohydrates = g.Carbohydrates,
                Fat = g.Fat,
                DateSet = g.DateSet
            }));
        }

        [HttpGet("summary")]
        public IActionResult GetGoalSummaries()
        {
            if (!Request.Headers.TryGetValue("UserId", out var userIdHeader) ||
                !int.TryParse(userIdHeader, out var userId) ||
                userId <= 0)
            {
                return BadRequest(new SimpleMessageDto
                {
                    Message = "UserId header je obavezan."
                });
            }

            var user = _userService.GetUserById(userId);
            if (user == null)
            {
                return NotFound(new SimpleMessageDto
                {
                    Message = "Korisnik nije pronađen."
                });
            }

            var summaries = _goalService.GetGoalSummariesForUser(userId);
            return Ok(summaries);
        }
    }
}