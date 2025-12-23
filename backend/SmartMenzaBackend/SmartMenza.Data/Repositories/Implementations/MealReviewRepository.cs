using Microsoft.EntityFrameworkCore;
using SmartMenza.Data.Context;
using SmartMenza.Data.Entities;
using SmartMenza.Data.Repositories.Interfaces;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SmartMenza.Data.Repositories.Implementations
{
    public class MealReviewRepository : IMealReviewRepository
    {
        private readonly SmartMenzaContext _context;

        public MealReviewRepository(SmartMenzaContext context)
        {
            _context = context;
        }

        public MealReview? GetById(int reviewId)
            => _context.MealReview
                .Include(r => r.User)
                .FirstOrDefault(r => r.MealReviewId == reviewId);

        public MealReview? GetByUserAndMeal(int userId, int mealId)
            => _context.MealReview
                .FirstOrDefault(r => r.UserId == userId && r.MealId == mealId);

        public List<MealReview> GetByMeal(int mealId)
            => _context.MealReview
                .Include(r => r.User)
                .Where(r => r.MealId == mealId)
                .OrderByDescending(r => r.UpdatedAt)
                .ToList();

        public Meal? GetMealForRatingUpdate(int mealId)
            => _context.Meal.FirstOrDefault(m => m.MealId == mealId);

        public void Add(MealReview review) => _context.MealReview.Add(review);

        public void Remove(MealReview review) => _context.MealReview.Remove(review);

        public void Save() => _context.SaveChanges();
    }
}