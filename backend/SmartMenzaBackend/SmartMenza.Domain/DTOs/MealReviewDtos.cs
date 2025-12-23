using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SmartMenza.Domain.DTOs
{
    public class MealReviewCreateDto
    {
        public int MealId { get; set; }
        public int Rating { get; set; }
        public string? Comment { get; set; }
    }
    public class MealReviewUpdateDto
    {
        public int Rating { get; set; }
        public string? Comment { get; set; }
    }
    public class MealReviewDto
    {
        public int MealReviewId { get; set; }
        public int MealId { get; set; }
        public int UserId { get; set; }
        public string Username { get; set; } = string.Empty;

        public int Rating { get; set; }
        public string? Comment { get; set; }

        public DateTime CreatedAt { get; set; }
        public DateTime UpdatedAt { get; set; }
    }
    public class MealRatingSummaryDto
    {
        public int MealId { get; set; }
        public decimal AverageRating { get; set; }
        public int RatingsCount { get; set; }
    }
}