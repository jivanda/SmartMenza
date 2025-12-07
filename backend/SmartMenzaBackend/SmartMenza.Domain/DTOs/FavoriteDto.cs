using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SmartMenza.Domain.DTOs
{
    public class FavoriteDto
    {
        public int MealId { get; set; }
        public string MealName { get; set; } = string.Empty;
        public string? ImageUrl { get; set; }
        public decimal? Calories { get; set; }
        public decimal? Protein { get; set; }
    }

    public class FavoriteToggleDto
    {
        public int MealId { get; set; }
    }
}