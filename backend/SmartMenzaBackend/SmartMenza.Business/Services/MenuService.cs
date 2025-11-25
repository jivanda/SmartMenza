using Microsoft.EntityFrameworkCore;
using SmartMenza.Data.Context.SmartMenza.Data.Context;
using SmartMenza.Domain.DTOs;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SmartMenza.Business.Services
{
    public class MenuService
    {
        private readonly SmartMenzaContext _context;

        public MenuService(SmartMenzaContext context)
        {
            _context = context;
        }

        /// <summary>
        /// Public funkcionalnost - svi korisnici mogu dohvatiti meni za datum.
        /// </summary>
        public MenuResponseDto? GetMenuByDate(DateTime date)
        {
            var menuDate = _context.MenuDate
                .Include(md => md.Menu)
                    .ThenInclude(m => m.MenuType)
                .Include(md => md.Menu)
                    .ThenInclude(m => m.MenuMeals)
                        .ThenInclude(mm => mm.Meal)
                .FirstOrDefault(md => md.Date == date.Date);

            if (menuDate == null)
                return null;

            var menu = menuDate.Menu;

            return new MenuResponseDto
            {
                MenuId = menu.MenuId,
                Name = menu.Name,
                Description = menu.Description,
                Date = menuDate.Date,
                MenuTypeName = menu.MenuType?.Name,
                Meals = menu.MenuMeals
                    .Select(mm => new MealDto
                    {
                        MealId = mm.Meal.MealId,
                        Name = mm.Meal.Name,
                        Description = mm.Meal.Description,
                        Price = mm.Meal.Price,
                        Calories = mm.Meal.Calories,
                        Protein = mm.Meal.Protein,
                        Carbohydrates = mm.Meal.Carbohydrates,
                        Fat = mm.Meal.Fat
                    })
                    .ToList()
            };
        }

        // ADMIN/ZAPOSLENIK FUNKCIONALNOSTI ĆE IĆI OVDE
    }
}