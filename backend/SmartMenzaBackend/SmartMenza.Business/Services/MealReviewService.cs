using SmartMenza.Data.Entities;
using SmartMenza.Data.Repositories.Interfaces;
using SmartMenza.Domain.DTOs;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SmartMenza.Business.Services
{
    public class MealReviewService
    {
        private readonly IMealReviewRepository _reviewRepo;

        public MealReviewService(IMealReviewRepository reviewRepo)
        {
            _reviewRepo = reviewRepo;
        }

        public (bool Success, string? ErrorMessage, MealReview? Review) Create(int userId, MealReviewCreateDto dto)
        {
            if (dto.MealId <= 0) return (false, "Neispravan MealId.", null);
            if (dto.Rating < 1 || dto.Rating > 5) return (false, "Ocjena mora biti između 1 i 5.", null);

            var meal = _reviewRepo.GetMealForRatingUpdate(dto.MealId);
            if (meal == null) return (false, "Jelo nije pronađeno.", null);

            if (_reviewRepo.GetByUserAndMeal(userId, dto.MealId) != null)
                return (false, "Korisnik je već ocijenio ovo jelo.", null);

            var now = DateTime.UtcNow;

            var review = new MealReview
            {
                UserId = userId,
                MealId = dto.MealId,
                Rating = dto.Rating,
                Comment = dto.Comment,
                CreatedAt = now,
                UpdatedAt = now
            };

            _reviewRepo.Add(review);

            var oldCount = meal.RatingsCount;
            var newCount = oldCount + 1;
            var newAvg = ((meal.AverageRating * oldCount) + dto.Rating) / newCount;

            meal.RatingsCount = newCount;
            meal.AverageRating = newAvg;

            _reviewRepo.Save();

            return (true, null, review);
        }

        public (bool Success, string? ErrorMessage, MealReview? Review) Update(int userId, int reviewId, MealReviewUpdateDto dto)
        {
            if (dto.Rating < 1 || dto.Rating > 5) return (false, "Ocjena mora biti između 1 i 5.", null);

            var review = _reviewRepo.GetById(reviewId);
            if (review == null) return (false, "Recenzija nije pronađena.", null);
            if (review.UserId != userId) return (false, "Nemate ovlasti za uređivanje ove recenzije.", null);

            var meal = _reviewRepo.GetMealForRatingUpdate(review.MealId);
            if (meal == null) return (false, "Jelo nije pronađeno.", null);

            var oldRating = review.Rating;
            var count = meal.RatingsCount;

            review.Rating = dto.Rating;
            review.Comment = dto.Comment;
            review.UpdatedAt = DateTime.UtcNow;

            if (count > 0)
            {
                var newAvg = ((meal.AverageRating * count) - oldRating + dto.Rating) / count;
                meal.AverageRating = newAvg;
            }

            _reviewRepo.Save();
            return (true, null, review);
        }

        public (bool Success, string? ErrorMessage) Delete(int userId, int reviewId)
        {
            var review = _reviewRepo.GetById(reviewId);
            if (review == null) return (false, "Recenzija nije pronađena.");
            if (review.UserId != userId) return (false, "Nemate ovlasti za brisanje ove recenzije.");

            var meal = _reviewRepo.GetMealForRatingUpdate(review.MealId);
            if (meal == null) return (false, "Jelo nije pronađeno.");

            var oldRating = review.Rating;
            var oldCount = meal.RatingsCount;
            var newCount = oldCount - 1;

            _reviewRepo.Remove(review);

            if (newCount <= 0)
            {
                meal.RatingsCount = 0;
                meal.AverageRating = 0;
            }
            else
            {
                var newAvg = ((meal.AverageRating * oldCount) - oldRating) / newCount;
                meal.RatingsCount = newCount;
                meal.AverageRating = newAvg;
            }

            _reviewRepo.Save();
            return (true, null);
        }

        public List<MealReview> GetByMeal(int mealId)
            => _reviewRepo.GetByMeal(mealId);
    }
}