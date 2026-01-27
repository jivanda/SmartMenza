using System;

namespace SmartMenza.Domain.DTOs
{
    public class NutritionAssessmentDto
    {
        public int MenuId { get; set; }

        public string Reasoning { get; set; } = string.Empty;
    }
}