using SmartMenza.Data.Entities;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SmartMenza.Data.Repositories.Interfaces
{
    public interface IMealReviewRepository
    {
        MealReview? GetById(int reviewId);
        MealReview? GetByUserAndMeal(int userId, int mealId);
        List<MealReview> GetByMeal(int mealId);

        Meal? GetMealForRatingUpdate(int mealId);

        void Add(MealReview review);
        void Remove(MealReview review);

        void Save();
    }
}