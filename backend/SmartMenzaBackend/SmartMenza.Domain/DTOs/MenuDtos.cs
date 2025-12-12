using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SmartMenza.Domain.DTOs
{
    public class MealDto
    {
        public int MealId { get; set; }
        public string Name { get; set; } = string.Empty;
        public string? Description { get; set; }
        public decimal Price { get; set; }

        public decimal? Calories { get; set; }
        public decimal? Protein { get; set; }
        public decimal? Carbohydrates { get; set; }
        public decimal? Fat { get; set; }
    }

    public class MenuResponseDto
    {
        public int MenuId { get; set; }
        public string Name { get; set; } = string.Empty;
        public string? Description { get; set; }
        public DateTime Date { get; set; }
        public string? MenuTypeName { get; set; }

        public List<MealDto> Meals { get; set; } = new();
    }

    public class MenuResponseDtoNoDate
    {
        public int MenuId { get; set; }
        public string Name { get; set; } = string.Empty;
        public string? Description { get; set; }
        public string? MenuTypeName { get; set; }

        public List<MealDto> Meals { get; set; } = new();
    }
}