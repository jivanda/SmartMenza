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
    public class RatingCommentRepository : IRatingCommentRepository
    {
        private readonly SmartMenzaContext _context;

        public RatingCommentRepository(SmartMenzaContext context)
        {
            _context = context;
        }

        public RatingComment? Get(int userId, int mealId)
            => _context.RatingComment
                .Include(r => r.User)
                .FirstOrDefault(r => r.UserId == userId && r.MealId == mealId);

        public bool Exists(int userId, int mealId)
            => _context.RatingComment.Any(r => r.UserId == userId && r.MealId == mealId);

        public List<RatingComment> GetByMeal(int mealId)
            => _context.RatingComment
                .Include(r => r.User)
                .Where(r => r.MealId == mealId)
                .ToList();

        public Meal? GetMeal(int mealId)
            => _context.Meal.FirstOrDefault(m => m.MealId == mealId);

        public void Add(RatingComment entity) => _context.RatingComment.Add(entity);

        public void Remove(RatingComment entity) => _context.RatingComment.Remove(entity);

        public void Save() => _context.SaveChanges();
    }
}