using SmartMenza.Data.Context;
using SmartMenza.Domain.Entities;
using Microsoft.EntityFrameworkCore;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SmartMenza.Business.Services
{
    public class FavoriteService
    {
        private readonly SmartMenzaContext _context;

        public FavoriteService(SmartMenzaContext context)
        {
            _context = context;
        }

        public bool AddFavorite(int userId, int mealId)
        {
            var exists = _context.Set<Favorite>().Any(f => f.UserId == userId && f.MealId == mealId);
            if (exists) return false;

            _context.Set<Favorite>().Add(new Favorite
            {
                UserId = userId,
                MealId = mealId
            });

            _context.SaveChanges();
            return true;
        }

        public bool RemoveFavorite(int userId, int mealId)
        {
            var fav = _context.Set<Favorite>().FirstOrDefault(f => f.UserId == userId && f.MealId == mealId);
            if (fav == null) return false;

            _context.Set<Favorite>().Remove(fav);
            _context.SaveChanges();
            return true;
        }

        public List<Favorite> GetFavorites(int userId)
        {
            return _context.Set<Favorite>()
                .Include(f => f.Meal)
                .Where(f => f.UserId == userId)
                .ToList();
        }

        public bool IsFavorite(int userId, int mealId)
        {
            return _context.Set<Favorite>().Any(f => f.UserId == userId && f.MealId == mealId);
        }
    }
}