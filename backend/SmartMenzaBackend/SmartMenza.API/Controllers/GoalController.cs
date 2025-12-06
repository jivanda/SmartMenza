using Microsoft.AspNetCore.Mvc;
using SmartMenza.Business.Services;
using SmartMenza.Domain.DTOs;

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
        [ProducesResponseType(typeof(object), StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        public IActionResult CreateGoal(
            [FromBody] GoalCreateDto goalDto,
            [FromHeader(Name = "UserId")] int userId)
        {
            if (goalDto.Calories <= 0 || goalDto.TargetProteins <= 0)
            {
                return BadRequest(new { message = "Vrijednosti cilja moraju biti veće od 0." });
            }

            var user = _userService.GetUserById(userId);
            if (user == null)
                return Unauthorized("Korisnik nije pronađen.");

            var createdGoal = _goalService.CreateGoal(goalDto, userId);

            var goalResult = new
            {
                createdGoal.GoalId,
                createdGoal.Calories,
                Protein = createdGoal.Protein,
                Carbohydrates = createdGoal.Carbohydrates,
                Fat = createdGoal.Fat,
                createdGoal.DateSet,
                createdGoal.UserId
            };

            return Ok(new { message = "Cilj je uspješno kreiran.", goal = goalResult });
        }

        [HttpPost("validate")]
        public IActionResult ValidateGoal([FromBody] GoalValidationDto goalDto)
        {
            if (goalDto.TargetCalories <= 0 || goalDto.TargetProteins <= 0)
            {
                return BadRequest(new { message = "Vrijednosti moraju biti pozitivne i realne." });
            }

            return Ok(new { message = "Cilj je valjan." });
        }

        [HttpPost("check-user")]
        public IActionResult CheckGoalOwnership(
            [FromBody] GoalOwnershipCheckDto checkDto,
            [FromHeader(Name = "UserId")] int userId)
        {
            var user = _userService.GetUserById(userId);
            if (user == null)
                return Unauthorized("Korisnik nije pronađen.");

            var goal = _goalService.GetGoalById(checkDto.GoalId);
            if (goal == null || goal.UserId != userId)
            {
                return Unauthorized("Ovaj cilj ne pripada trenutno prijavljenom korisniku.");
            }

            return Ok(new { message = "Vlasništvo cilja je uspješno potvrđeno." });
        }

        [HttpPut("{goalId}")]
        [ProducesResponseType(typeof(object), StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        [ProducesResponseType(StatusCodes.Status401Unauthorized)]
        public IActionResult UpdateGoal(
            int goalId,
            [FromBody] GoalUpdateDto dto,
            [FromHeader(Name = "UserId")] int userId)
        {
            var user = _userService.GetUserById(userId);
            if (user == null)
                return Unauthorized("Korisnik nije pronađen.");

            var result = _goalService.UpdateGoal(goalId, userId, dto);

            if (!result.Success)
                return BadRequest(new { message = result.ErrorMessage });

            var updated = result.UpdatedGoal!;

            var goalResult = new
            {
                updated.GoalId,
                updated.Calories,
                Protein = updated.Protein,
                Carbohydrates = updated.Carbohydrates,
                Fat = updated.Fat,
                updated.DateSet,
                updated.UserId
            };

            return Ok(new
            {
                message = "Cilj je uspješno ažuriran.",
                goal = goalResult
            });
        }

        [HttpDelete("{goalId}")]
        [ProducesResponseType(typeof(object), StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        [ProducesResponseType(StatusCodes.Status401Unauthorized)]
        public IActionResult DeleteGoal(
            int goalId,
            [FromHeader(Name = "UserId")] int userId)
        {
            var user = _userService.GetUserById(userId);
            if (user == null)
                return Unauthorized("Korisnik nije pronađen.");

            var result = _goalService.DeleteGoal(goalId, userId);

            if (!result.Success)
                return BadRequest(new { message = result.ErrorMessage });

            return Ok(new { message = "Cilj je uspješno obrisan." });
        }

        [HttpGet("myGoal")]
        [ProducesResponseType(typeof(List<GoalDto>), StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public IActionResult GetMyGoals([FromHeader(Name = "UserId")] int userId)
        {
            if (userId <= 0)
                return BadRequest(new { message = "UserId header je obavezan." });

            var user = _userService.GetUserById(userId);
            if (user == null)
                return NotFound(new { message = "Korisnik nije pronađen." });

            var goals = _goalService.GetGoalsByUser(userId);

            if (goals == null || !goals.Any())
                return NotFound(new { message = "Korisnik nema spremljenih ciljeva." });

            var mapped = goals.Select(g => new GoalDto
            {
                GoalId = g.GoalId,
                Calories = g.Calories,
                Protein = g.Protein,
                Carbohydrates = g.Carbohydrates,
                Fat = g.Fat,
                DateSet = g.DateSet
            }).ToList();

            return Ok(mapped);
        }

        [HttpGet("summary")]
        [ProducesResponseType(typeof(GoalSummaryDto), StatusCodes.Status200OK)]
        [ProducesResponseType(StatusCodes.Status400BadRequest)]
        [ProducesResponseType(StatusCodes.Status404NotFound)]
        public IActionResult GetGoalSummaries([FromHeader(Name = "UserId")] int userId)
        {
            if (userId <= 0)
                return BadRequest(new { message = "UserId header je obavezan." });

            var user = _userService.GetUserById(userId);
            if (user == null)
                return NotFound(new { message = "Korisnik nije pronađen." });

            var summaries = _goalService.GetGoalSummariesForUser(userId);

            if (summaries == null)
                return NotFound(new { message = "Nema dostupnih podataka o ciljevima." });

            return Ok(summaries);
        }
    }
}