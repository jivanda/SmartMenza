using Microsoft.AspNetCore.Mvc;
using SmartMenza.Business.Services;
using SmartMenza.Domain.DTOs;
using SmartMenza.Domain.Entities;
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
        public IActionResult CreateGoal([FromBody] GoalCreateDto goalDto, [FromHeader(Name = "UserId")] int userId)
        {
            if (goalDto.Calories <= 0 || goalDto.TargetProteins <= 0)
            {
                return BadRequest(new { message = "Goal values should be greater than 0." });
            }

            var user = _userService.GetUserById(userId);
            if (user == null)
                return Unauthorized("User not found");

            var createdGoal = _goalService.CreateGoal(goalDto, userId);

            if (createdGoal == null)
                return BadRequest("Failed to create goal.");

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

            return Ok(new { message = "Goal successfully created.", goal = goalResult });
        }

        [HttpPost("validate")]
        public IActionResult ValidateGoal([FromBody] GoalValidationDto goalDto)
        {
            if (goalDto.TargetCalories <= 0 || goalDto.TargetProteins <= 0)
            {
                return BadRequest(new { message = "The values must be positive and real." });
            }

            return Ok(new { message = "Goal is valid." });
        }

        [HttpPost("check-user")]
        public IActionResult CheckGoalOwnership([FromBody] GoalOwnershipCheckDto checkDto, [FromHeader(Name = "UserId")] int userId)
        {
            var user = _userService.GetUserById(userId);
            if (user == null)
                return Unauthorized("User not found");

            var goal = _goalService.GetGoalById(checkDto.GoalId);
            if (goal == null || goal.UserId != userId)
            {
                return Unauthorized("This goal does not belong to the current user.");
            }

            return Ok(new { message = "Goal ownership validated successfully." });
        }
    }
}