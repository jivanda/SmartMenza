using Microsoft.EntityFrameworkCore;
using SmartMenza.Data.Context;
using SmartMenza.Domain.DTOs;
using SmartMenza.Domain.Entities;
using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;

namespace SmartMenza.Business.Services
{
    public class MenuService
    {
        private readonly SmartMenzaContext _context;

        public MenuService(SmartMenzaContext context)
        {
            _context = context;
        }

        public MenuResponseDto? GetMenuByDate(string date)
        {
            if (!DateTime.TryParseExact(
                    date,
                    "dd/MM/yyyy",
                    CultureInfo.InvariantCulture,
                    DateTimeStyles.None,
                    out var parsedDate))
            {
                return null;
            }

            return GetMenuByDate(parsedDate);
        }

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
                    .Where(mm => mm.Meal != null)
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

        public List<MenuResponseDto>? GetMenusByDate(string date)
        {
            if (!DateTime.TryParseExact(
                    date,
                    "dd/MM/yyyy",
                    CultureInfo.InvariantCulture,
                    DateTimeStyles.None,
                    out var parsedDate))
            {
                return null;
            }

            return GetMenusByDate(parsedDate);
        }

        public List<MenuResponseDto> GetMenusByDate(DateTime date)
        {
            var menuDates = _context.MenuDate
                .Include(md => md.Menu)
                    .ThenInclude(m => m.MenuType)
                .Include(md => md.Menu)
                    .ThenInclude(m => m.MenuMeals)
                        .ThenInclude(mm => mm.Meal)
                .Where(md => md.Date == date.Date)
                .ToList();

            var result = menuDates
                .Select(md =>
                {
                    var menu = md.Menu;

                    return new MenuResponseDto
                    {
                        MenuId = menu.MenuId,
                        Name = menu.Name,
                        Description = menu.Description,
                        Date = md.Date,
                        MenuTypeName = menu.MenuType?.Name,
                        Meals = (menu.MenuMeals ?? Enumerable.Empty<MenuMeal>())
                            .Where(mm => mm.Meal != null)
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
                })
                .ToList();

            return result;
        }

        public (bool Success, string? ErrorMessage, int? MenuId) CreateMenuForDate(CreateMenuDto dto)
        {
            if (!DateTime.TryParseExact(
                    dto.Date,
                    "dd/MM/yyyy",
                    CultureInfo.InvariantCulture,
                    DateTimeStyles.None,
                    out var parsedDate))
            {
                return (false, "Nevažeći format datuma. Očekivano dd/MM/gggg", null);
            }

            parsedDate = parsedDate.Date;

            if (dto.Meals == null || !dto.Meals.Any())
            {
                return (false, "Jelovnik mora sadržavati barem jedno jelo", null);
            }

            //var existsForDate = _context.MenuDate.Any(md => md.Date == parsedDate);
            //if (existsForDate)
            //{
            //    return (false, "Jelovnik za odabrani datum već postoji", null);
            //}

            var menu = new Domain.Entities.Menu
            {
                Name = dto.Name,
                Description = dto.Description,
                MenuTypeId = dto.MenuTypeId
            };

            _context.Menu.Add(menu);
            _context.SaveChanges();

            var menuDate = new Domain.Entities.MenuDate
            {
                MenuId = menu.MenuId,
                Date = parsedDate
            };
            _context.MenuDate.Add(menuDate);

            foreach (var mealDto in dto.Meals)
            {
                var menuMeal = new Domain.Entities.MenuMeal
                {
                    MenuId = menu.MenuId,
                    MealId = mealDto.MealId
                };
                _context.MenuMeal.Add(menuMeal);
            }

            _context.SaveChanges();

            return (true, null, menu.MenuId);
        }

        public (bool Success, string? ErrorMessage) UpdateMenu(int menuId, UpdateMenuDto dto)
        {
            var menu = _context.Menu
                .Include(m => m.MenuMeals)
                .FirstOrDefault(m => m.MenuId == menuId);

            if (menu == null)
            {
                return (false, "Jelovnik nije pronađen");
            }

            if (dto.Meals == null || !dto.Meals.Any())
            {
                return (false, "Jelovnik mora sadržavati barem jedno jelo.");
            }

            menu.Name = dto.Name;
            menu.Description = dto.Description;
            menu.MenuTypeId = dto.MenuTypeId;

            var existingLinks = menu.MenuMeals.ToList();
            _context.MenuMeal.RemoveRange(existingLinks);

            foreach (var mealItem in dto.Meals)
            {
                var exists = _context.Meal.Any(m => m.MealId == mealItem.MealId);
                if (!exists)
                {
                    return (false, $"Jelovnik s Id-om {mealItem.MealId} nije pronađen");
                }

                _context.MenuMeal.Add(new MenuMeal
                {
                    MenuId = menu.MenuId,
                    MealId = mealItem.MealId
                });
            }

            _context.SaveChanges();
            return (true, null);
        }

        public (bool Success, string? ErrorMessage) DeleteMenu(int menuId)
        {
            var menu = _context.Menu
                .Include(m => m.MenuDates)
                .Include(m => m.MenuMeals)
                .FirstOrDefault(m => m.MenuId == menuId);

            if (menu == null)
                return (false, "Jelovnik nije pronađen.");

            _context.MenuDate.RemoveRange(menu.MenuDates);
            _context.MenuMeal.RemoveRange(menu.MenuMeals);
            _context.Menu.Remove(menu);

            _context.SaveChanges();
            return (true, null);
        }

    }
}