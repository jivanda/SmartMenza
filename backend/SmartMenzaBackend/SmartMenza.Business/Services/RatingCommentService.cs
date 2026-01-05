using SmartMenza.Data.Entities;
using SmartMenza.Data.Repositories.Interfaces;
using SmartMenza.Domain.DTOs;
using System.Collections.Generic;
using System.Linq;

namespace SmartMenza.Business.Services
{
    public class RatingCommentService
    {
        private readonly IRatingCommentRepository _repo;

        public RatingCommentService(IRatingCommentRepository repo)
        {
            _repo = repo;
        }

        public (bool Success, string? ErrorMessage) Create(int userId, RatingCommentCreateDto dto)
        {
            if (dto.MealId <= 0) return (false, "Neispravan MealId.");
            if (dto.Rating < 1 || dto.Rating > 5) return (false, "Ocjena mora biti između 1 i 5.");

            if (_repo.GetMeal(dto.MealId) == null)
                return (false, "Jelo nije pronađeno.");

            if (_repo.Exists(userId, dto.MealId))
                return (false, "Korisnik je već ocijenio ovo jelo.");

            _repo.Add(new RatingComment
            {
                UserId = userId,
                MealId = dto.MealId,
                Rating = dto.Rating,
                Comment = dto.Comment
            });

            _repo.Save();
            return (true, null);
        }

        public (bool Success, string? ErrorMessage) Update(int userId, int mealId, RatingCommentUpdateDto dto)
        {
            if (dto.Rating < 1 || dto.Rating > 5) return (false, "Ocjena mora biti između 1 i 5.");

            var rc = _repo.Get(userId, mealId);
            if (rc == null) return (false, "Recenzija nije pronađena.");

            rc.Rating = dto.Rating;
            rc.Comment = dto.Comment;

            _repo.Save();
            return (true, null);
        }

        public (bool Success, string? ErrorMessage) Delete(int userId, int mealId)
        {
            var rc = _repo.Get(userId, mealId);
            if (rc == null) return (false, "Recenzija nije pronađena.");

            _repo.Remove(rc);
            _repo.Save();
            return (true, null);
        }

        public List<RatingComment> GetByMeal(int mealId) => _repo.GetByMeal(mealId);

        public RatingSummaryDto GetSummary(int mealId)
        {
            var list = _repo.GetByMeal(mealId);
            var count = list.Count;
            var avg = count == 0 ? 0 : (decimal)list.Average(x => x.Rating);

            return new RatingSummaryDto
            {
                MealId = mealId,
                RatingsCount = count,
                AverageRating = avg
            };
        }
        public int HasUserReviewedMeal(int userId, int mealId)
        {
            if (userId <= 0 || mealId <= 0) return 0;
            return _repo.Exists(userId, mealId) ? 1 : 0;
        }
    }
}