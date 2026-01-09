using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using SmartMenza.Data.Entities;

namespace SmartMenza.Data.Repositories.Interfaces
{
    public interface IMealTypeRepository
    {
        List<MealType> GetAll();
        MealType? GetById(int mealTypeId);
    }
}
