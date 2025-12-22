using SmartMenza.Data.Entities;
using System.Collections.Generic;

namespace SmartMenza.Data.Repositories.Interfaces
{
    public interface IFavoriteRepository
    {
        Favorite? Get(int userId, int mealId);
        List<Favorite> GetByUser(int userId);
        bool Exists(int userId, int mealId);
        void Add(Favorite favorite);
        void Remove(Favorite favorite);
        void Save();
    }
}