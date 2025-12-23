using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SmartMenza.Data.Entities
{
    public class MealReview
    {
        public int MealReviewId { get; set; }

        public int UserId { get; set; }
        public UserAccount User { get; set; } = null!;

        public int MealId { get; set; }
        public Meal Meal { get; set; } = null!;

        public int Rating { get; set; }
        public string? Comment { get; set; }

        public DateTime CreatedAt { get; set; }
        public DateTime UpdatedAt { get; set; }
    }
}