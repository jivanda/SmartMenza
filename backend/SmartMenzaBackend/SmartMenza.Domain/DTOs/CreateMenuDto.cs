using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SmartMenza.Domain.DTOs
{
    public class MenuMealItemDto
    {
        public int MealId { get; set; }
    }

    public class CreateMenuDto
    {
        public string Name { get; set; } = string.Empty;
        public string? Description { get; set; }
        public string Date { get; set; } = string.Empty;
        public int MenuTypeId { get; set; }
        public List<MenuMealItemDto> Meals { get; set; } = new();
    }
}