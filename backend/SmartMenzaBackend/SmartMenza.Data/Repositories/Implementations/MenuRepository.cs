using Microsoft.EntityFrameworkCore;
using SmartMenza.Data.Context;
using SmartMenza.Data.Entities;
using SmartMenza.Data.Repositories.Interfaces;
using System.Collections.Generic;
using System.Linq;

namespace SmartMenza.Data.Repositories.Implementations
{
    public class MenuRepository : IMenuRepository
    {
        private readonly SmartMenzaContext _context;

        public MenuRepository(SmartMenzaContext context)
        {
            _context = context;
        }

        public MenuDate? GetMenuByDate(DateOnly date)
            => _context.MenuDate
                .Include(md => md.Menu)
                    .ThenInclude(m => m.MenuType)
                .Include(md => md.Menu)
                    .ThenInclude(m => m.MenuMeals)
                        .ThenInclude(mm => mm.Meal)
                .FirstOrDefault(md => md.Date == date);

        public List<MenuDate> GetMenusByDate(DateOnly date)
            => _context.MenuDate
                .Include(md => md.Menu)
                    .ThenInclude(m => m.MenuType)
                .Include(md => md.Menu)
                    .ThenInclude(m => m.MenuMeals)
                        .ThenInclude(mm => mm.Meal)
                .Where(md => md.Date == date)
                .ToList();

        public Menu? GetMenuById(int menuId)
            => _context.Menu
                .Include(m => m.MenuType)
                .Include(m => m.MenuMeals)
                    .ThenInclude(mm => mm.Meal)
                .FirstOrDefault(m => m.MenuId == menuId);

        public List<Menu> GetMenusByType(int menuTypeId)
            => _context.Menu
                .Include(m => m.MenuType)
                .Include(m => m.MenuMeals)
                    .ThenInclude(mm => mm.Meal)
                .Where(m => m.MenuTypeId == menuTypeId)
                .ToList();

        public void AddMenu(Menu menu) => _context.Menu.Add(menu);
        public void AddMenuDate(MenuDate menuDate) => _context.MenuDate.Add(menuDate);
        public void AddMenuMeal(MenuMeal menuMeal) => _context.MenuMeal.Add(menuMeal);

        public void RemoveMenu(Menu menu) => _context.Menu.Remove(menu);
        public void RemoveMenuDates(IEnumerable<MenuDate> menuDates) => _context.MenuDate.RemoveRange(menuDates);
        public void RemoveMenuMeals(IEnumerable<MenuMeal> menuMeals) => _context.MenuMeal.RemoveRange(menuMeals);

        public void Save() => _context.SaveChanges();
    }
}