using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SmartMenza.Data.Entities
{
    public class RatingComment
    {
        public int UserId { get; set; }
        public int MealId { get; set; }

        public int Rating { get; set; }
        public string? Comment { get; set; }

        public UserAccount User { get; set; } = null!;
        public Meal Meal { get; set; } = null!;
    }
}