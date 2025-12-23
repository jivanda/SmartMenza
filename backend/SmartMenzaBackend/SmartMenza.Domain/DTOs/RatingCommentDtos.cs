using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SmartMenza.Domain.DTOs
{
    public class RatingCommentCreateDto
    {
        public int MealId { get; set; }
        public int Rating { get; set; }
        public string? Comment { get; set; }
    }

    public class RatingCommentUpdateDto
    {
        public int Rating { get; set; }
        public string? Comment { get; set; }
    }

    public class RatingCommentDto
    {
        public int MealId { get; set; }
        public int UserId { get; set; }
        public string Username { get; set; } = string.Empty;
        public int Rating { get; set; }
        public string? Comment { get; set; }
    }

    public class RatingSummaryDto
    {
        public int MealId { get; set; }
        public decimal AverageRating { get; set; }
        public int RatingsCount { get; set; }
    }
}