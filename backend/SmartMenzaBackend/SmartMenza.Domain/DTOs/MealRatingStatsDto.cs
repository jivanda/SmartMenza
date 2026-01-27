using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SmartMenza.Domain.DTOs
{
    public class MealRatingStatsDto
    {
        public int MealId { get; set; }
        public int NumberOfReviews { get; set; }
        public decimal AverageRating { get; set; }
    }
}