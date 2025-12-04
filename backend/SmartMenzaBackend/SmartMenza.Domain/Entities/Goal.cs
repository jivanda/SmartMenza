using System;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace SmartMenza.Domain.Entities
{
    [Table("NutritionGoal")]
    public class NutritionGoal
    {
        [Key]
        public int GoalId { get; set; }
        public decimal Calories { get; set; }
        public decimal Protein { get; set; }
        public decimal Carbohydrates { get; set; }
        public decimal Fat { get; set; }

        public DateTime? DateSet { get; set; }

        public int UserId { get; set; }
        public UserAccount User { get; set; }
    }
}