using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using SmartMenza.Data.Repositories.Implementations;
using SmartMenza.Data.Repositories.Interfaces;
using SmartMenza.Domain.DTOs;

namespace SmartMenza.Business.Services
{
    public class MealTypeService
    {
        private readonly IMealTypeRepository _mealTypeRepository;

        public MealTypeService(IMealTypeRepository mealTypeRepository)
        {
            _mealTypeRepository = mealTypeRepository;
        }

        public String? GetNameById(int mealTypeId)
        {
            var m = _mealTypeRepository.GetById(mealTypeId);

            if (m == null)
                return null;

            return m.Name;
        }
    }
}
