using SmartMenza.Data.Entities;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SmartMenza.Data.Repositories.Interfaces
{
    public interface IRatingCommentRepository
    {
        RatingComment? Get(int userId, int mealId);
        bool Exists(int userId, int mealId);
        List<RatingComment> GetByMeal(int mealId);

        Meal? GetMeal(int mealId);

        void Add(RatingComment entity);
        void Remove(RatingComment entity);
        void Save();
        List<RatingComment> GetAll();
    }
}