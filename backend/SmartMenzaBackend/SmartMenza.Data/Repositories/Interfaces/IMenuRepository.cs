using SmartMenza.Data.Entities;
using System.Collections.Generic;

namespace SmartMenza.Data.Repositories.Interfaces
{
    public interface IMenuRepository
    {
        MenuDate? GetMenuByDate(DateOnly date);
        List<MenuDate> GetMenusByDate(DateOnly date);
        Menu? GetMenuById(int menuId);
        List<Menu> GetMenusByType(int menuTypeId);

        void AddMenu(Menu menu);
        void AddMenuDate(MenuDate menuDate);
        void AddMenuMeal(MenuMeal menuMeal);

        void RemoveMenu(Menu menu);
        void RemoveMenuDates(IEnumerable<MenuDate> menuDates);
        void RemoveMenuMeals(IEnumerable<MenuMeal> menuMeals);

        void Save();
    }
}