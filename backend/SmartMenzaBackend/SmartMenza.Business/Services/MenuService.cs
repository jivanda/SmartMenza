using SmartMenza.Data.Repositories.Interfaces;
using SmartMenza.Domain.DTOs;
using SmartMenza.Data.Entities;
using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;

namespace SmartMenza.Business.Services
{
    public class MenuService
    {
        private readonly IMenuRepository _menuRepository;

        public MenuService(IMenuRepository menuRepository)
        {
            _menuRepository = menuRepository;
        }

        public MenuResponseDto? GetMenuByDate(string date)
        {
            if (!DateTime.TryParseExact(date, "dd/MM/yyyy", CultureInfo.InvariantCulture, DateTimeStyles.None, out var parsed))
                return null;

            return GetMenuByDate(DateOnly.FromDateTime(parsed));
        }

        public MenuResponseDto? GetMenuByDate(DateOnly date)
        {
            var menuDate = _menuRepository.GetMenuByDate(date);
            if (menuDate == null)
                return null;

            var menu = menuDate.Menu;

            return new MenuResponseDto
            {
                MenuId = menu.MenuId,
                Name = menu.Name,
                Description = menu.Description,
                Date = menuDate.Date.ToDateTime(TimeOnly.MinValue),
                MenuTypeName = menu.MenuType?.Name,
                Meals = menu.MenuMeals.Select(mm => new MealDto
                {
                    MealId = mm.Meal.MealId,
                    MealTypeId = mm.Meal.MealTypeId,
                    Name = mm.Meal.Name,
                    Description = mm.Meal.Description,
                    Price = mm.Meal.Price,
                    Calories = mm.Meal.Calories,
                    Protein = mm.Meal.Protein,
                    Carbohydrates = mm.Meal.Carbohydrates,
                    Fat = mm.Meal.Fat,
                    ImageUrl = mm.Meal.ImageUrl
                }).ToList()
            };
        }

        public List<MenuResponseDto> GetMenusByDate(DateOnly date)
        {
            return _menuRepository.GetMenusByDate(date)
                .Select(md => new MenuResponseDto
                {
                    MenuId = md.Menu.MenuId,
                    Name = md.Menu.Name,
                    Description = md.Menu.Description,
                    Date = md.Date.ToDateTime(TimeOnly.MinValue),
                    MenuTypeName = md.Menu.MenuType!.Name,
                    Meals = md.Menu.MenuMeals.Select(mm => new MealDto
                    {
                        MealId = mm.Meal.MealId,
                        MealTypeId = mm.Meal.MealTypeId,
                        Name = mm.Meal.Name,
                        Description = mm.Meal.Description,
                        Price = mm.Meal.Price,
                        Calories = mm.Meal.Calories,
                        Protein = mm.Meal.Protein,
                        Carbohydrates = mm.Meal.Carbohydrates,
                        Fat = mm.Meal.Fat
                    }).ToList()
                })
                .ToList();
        }


        public List<MenuResponseDto> GetMenusByType(int menuTypeId)
        {
            return _menuRepository.GetMenusByType(menuTypeId)
                .Select(menu => new MenuResponseDto
                {
                    MenuId = menu.MenuId,
                    Name = menu.Name,
                    Description = menu.Description,
                    MenuTypeName = menu.MenuType?.Name,
                    Meals = menu.MenuMeals
                        .Where(mm => mm.Meal != null)
                        .Select(mm => new MealDto
                        {
                            MealId = mm.Meal.MealId,
                            MealTypeId = mm.Meal.MealTypeId,
                            Name = mm.Meal.Name,
                            Description = mm.Meal.Description,
                            Price = mm.Meal.Price,
                            Calories = mm.Meal.Calories,
                            Protein = mm.Meal.Protein,
                            Carbohydrates = mm.Meal.Carbohydrates,
                            Fat = mm.Meal.Fat
                        })
                        .ToList()
                })
                .ToList();
        }

        public MenuResponseDtoNoDate? GetMenuById(int id)
        {
            var menu = _menuRepository.GetMenuById(id);
            if (menu == null)
                return null;

            return new MenuResponseDtoNoDate
            {
                MenuId = menu.MenuId,
                Name = menu.Name,
                Description = menu.Description,
                MenuTypeName = menu.MenuType?.Name,
                Meals = menu.MenuMeals.Select(mm => new MealDto
                {
                    MealId = mm.Meal.MealId,
                    MealTypeId = mm.Meal.MealTypeId,
                    Name = mm.Meal.Name,
                    Description = mm.Meal.Description,
                    Price = mm.Meal.Price,
                    Calories = mm.Meal.Calories,
                    Protein = mm.Meal.Protein,
                    Carbohydrates = mm.Meal.Carbohydrates,
                    Fat = mm.Meal.Fat,
                    ImageUrl = mm.Meal.ImageUrl
                }).ToList()
            };
        }

        public (bool Success, string? ErrorMessage, int? MenuId) CreateMenuForDate(CreateMenuDto dto)
        {
            if (!DateTime.TryParseExact(dto.Date, "dd/MM/yyyy", CultureInfo.InvariantCulture, DateTimeStyles.None, out var parsed))
                return (false, "Nevažeći format datuma.", null);

            if (!dto.Meals.Any())
                return (false, "Jelovnik mora sadržavati barem jedno jelo.", null);

            var menu = new Menu
            {
                Name = dto.Name,
                Description = dto.Description,
                MenuTypeId = dto.MenuTypeId
            };

            _menuRepository.AddMenu(menu);
            _menuRepository.Save();

            _menuRepository.AddMenuDate(new MenuDate
            {
                MenuId = menu.MenuId,
                Date = DateOnly.FromDateTime(parsed)
            });

            foreach (var meal in dto.Meals)
            {
                _menuRepository.AddMenuMeal(new MenuMeal
                {
                    MenuId = menu.MenuId,
                    MealId = meal.MealId
                });
            }

            _menuRepository.Save();
            return (true, null, menu.MenuId);
        }

        public (bool Success, string? ErrorMessage, int? MenuId) CreateMenu(CreateMenuDtoNoDate dto)
        {
            if (!dto.Meals.Any())
                return (false, "Jelovnik mora sadržavati barem jedno jelo.", null);

            var menu = new Menu
            {
                Name = dto.Name,
                Description = dto.Description,
                MenuTypeId = dto.MenuTypeId
            };

            _menuRepository.AddMenu(menu);
            _menuRepository.Save();

            foreach (var meal in dto.Meals)
            {
                _menuRepository.AddMenuMeal(new MenuMeal
                {
                    MenuId = menu.MenuId,
                    MealId = meal.MealId
                });
            }

            _menuRepository.Save();
            return (true, null, menu.MenuId);
        }

        public (bool Success, string? ErrorMessage) UpdateMenu(int menuId, UpdateMenuDto dto)
        {
            var menu = _menuRepository.GetMenuById(menuId);
            if (menu == null)
                return (false, "Jelovnik nije pronađen.");

            if (!dto.Meals.Any())
                return (false, "Jelovnik mora sadržavati barem jedno jelo.");

            menu.Name = dto.Name;
            menu.Description = dto.Description;
            menu.MenuTypeId = dto.MenuTypeId;

            _menuRepository.RemoveMenuMeals(menu.MenuMeals);

            foreach (var meal in dto.Meals)
            {
                _menuRepository.AddMenuMeal(new MenuMeal
                {
                    MenuId = menu.MenuId,
                    MealId = meal.MealId
                });
            }

            _menuRepository.Save();
            return (true, null);
        }


        public (bool Success, string? ErrorMessage) DeleteMenu(int menuId)
        {
            var menu = _menuRepository.GetMenuById(menuId);
            if (menu == null)
                return (false, "Jelovnik nije pronađen.");

            _menuRepository.RemoveMenuDates(menu.MenuDates);
            _menuRepository.RemoveMenuMeals(menu.MenuMeals);
            _menuRepository.RemoveMenu(menu);
            _menuRepository.Save();

            return (true, null);
        }
    }
}