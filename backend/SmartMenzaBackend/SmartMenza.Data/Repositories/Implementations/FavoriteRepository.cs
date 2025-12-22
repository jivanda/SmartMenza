using Microsoft.EntityFrameworkCore;
using SmartMenza.Data.Repositories.Interfaces;
using SmartMenza.Data.Context;
using SmartMenza.Data.Entities;
using System.Collections.Generic;
using System.Linq;

namespace SmartMenza.Data.Repositories.Implementations
{
    public class FavoriteRepository : IFavoriteRepository
    {
        private readonly SmartMenzaContext _context;

        public FavoriteRepository(SmartMenzaContext context)
        {
            _context = context;
        }

        public Favorite? Get(int userId, int mealId)
            => _context.Favorite
                .Include(f => f.Meal)
                .FirstOrDefault(f => f.UserId == userId && f.MealId == mealId);

        public List<Favorite> GetByUser(int userId)
            => _context.Favorite
                .Include(f => f.Meal)
                .Where(f => f.UserId == userId)
                .ToList();

        public bool Exists(int userId, int mealId)
            => _context.Favorite.Any(f => f.UserId == userId && f.MealId == mealId);

        public void Add(Favorite favorite)
            => _context.Favorite.Add(favorite);

        public void Remove(Favorite favorite)
            => _context.Favorite.Remove(favorite);

        public void Save()
            => _context.SaveChanges();
    }
}