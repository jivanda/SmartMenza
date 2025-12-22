using SmartMenza.Data.Entities;
using SmartMenza.Data.Repositories.Interfaces;
using System.Collections.Generic;

namespace SmartMenza.Business.Services
{
    public class FavoriteService
    {
        private readonly IFavoriteRepository _favoriteRepository;

        public FavoriteService(IFavoriteRepository favoriteRepository)
        {
            _favoriteRepository = favoriteRepository;
        }

        public bool AddFavorite(int userId, int mealId)
        {
            if (_favoriteRepository.Exists(userId, mealId))
                return false;

            _favoriteRepository.Add(new Favorite
            {
                UserId = userId,
                MealId = mealId
            });

            _favoriteRepository.Save();
            return true;
        }

        public bool RemoveFavorite(int userId, int mealId)
        {
            var favorite = _favoriteRepository.Get(userId, mealId);
            if (favorite == null)
                return false;

            _favoriteRepository.Remove(favorite);
            _favoriteRepository.Save();
            return true;
        }

        public List<Favorite> GetFavorites(int userId)
        {
            return _favoriteRepository.GetByUser(userId);
        }

        public bool IsFavorite(int userId, int mealId)
        {
            return _favoriteRepository.Exists(userId, mealId);
        }
    }
}