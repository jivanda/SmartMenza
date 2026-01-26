using System;

namespace SmartMenza.Domain.DTOs
{
    public class NutritionResultDto
    {
        public decimal Calories { get; set; }
        public decimal Proteins { get; set; }
        public decimal Carbohydrates { get; set; }
        public decimal Fats { get; set; }
    }
}