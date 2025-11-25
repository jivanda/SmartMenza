using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SmartMenza.Domain.Entities
{
    public class MealType
    {
        public int MealTypeId { get; set; }
        public string Name { get; set; } = string.Empty;

        public List<Meal> Meals { get; set; } = new();
    }

    public class Meal
    {
        public int MealId { get; set; }
        public string Name { get; set; } = string.Empty;
        public string? Description { get; set; }
        public decimal Price { get; set; }
        public int MealTypeId { get; set; }
        public MealType MealType { get; set; } = null!;

        public decimal? Calories { get; set; }
        public decimal? Protein { get; set; }
        public decimal? Carbohydrates { get; set; }
        public decimal? Fat { get; set; }
        public string? ImageUrl { get; set; }

        public List<MenuMeal> MenuMeals { get; set; } = new();
    }

    public class MenuType
    {
        public int MenuTypeId { get; set; }
        public string Name { get; set; } = string.Empty;

        public List<Menu> Menus { get; set; } = new();
    }

    public class Menu
    {
        public int MenuId { get; set; }
        public int? MenuTypeId { get; set; }
        public string Name { get; set; } = string.Empty;
        public string? Description { get; set; }

        public MenuType? MenuType { get; set; }

        public List<MenuDate> MenuDates { get; set; } = new();
        public List<MenuMeal> MenuMeals { get; set; } = new();
    }

    public class MenuDate
    {
        public int MenuId { get; set; }
        public DateTime Date { get; set; }

        public Menu Menu { get; set; } = null!;
    }

    public class MenuMeal
    {
        public int MenuId { get; set; }
        public int MealId { get; set; }

        public Menu Menu { get; set; } = null!;
        public Meal Meal { get; set; } = null!;
    }
}