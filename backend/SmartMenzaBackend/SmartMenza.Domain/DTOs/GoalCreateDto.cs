using System;

namespace SmartMenza.Domain.DTOs
{
    public class GoalCreateDto
    {
        public decimal Calories { get; set; }
        public decimal TargetProteins { get; set; }
        public decimal TargetCarbs { get; set; }
        public decimal TargetFats { get; set; }
    }

    public class GoalUpdateDto
    {
        public decimal Calories { get; set; }
        public decimal TargetProteins { get; set; }
        public decimal TargetCarbs { get; set; }
        public decimal TargetFats { get; set; }
    }

    public class GoalValidationDto
    {
        public decimal TargetCalories { get; set; }
        public decimal TargetProteins { get; set; }
        public decimal TargetCarbs { get; set; }
        public decimal TargetFats { get; set; }
    }

    public class GoalOwnershipCheckDto
    {
        public int GoalId { get; set; }
    }
}