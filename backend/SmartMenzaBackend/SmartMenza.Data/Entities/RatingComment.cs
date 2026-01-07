using System;

namespace SmartMenza.Data.Entities
{
    public class RatingComment
    {
        public int RatingId { get; set; }

        public int UserId { get; set; }
        public int MealId { get; set; }

        public int Rating { get; set; }
        public string? Comment { get; set; }

        public DateTime Date { get; set; }

        public UserAccount User { get; set; } = null!;
        public Meal Meal { get; set; } = null!;
    }
}