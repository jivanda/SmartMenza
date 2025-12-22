using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SmartMenza.Domain.DTOs
{
    public class FavoriteStatusDto
    {
        public int MealId { get; set; }
        public bool IsFavorite { get; set; }
    }
}