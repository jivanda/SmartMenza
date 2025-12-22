using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using SmartMenza.Data.Entities;

namespace SmartMenza.Data.Entities
{
    public class Favorite
    {
        public int UserId { get; set; }
        public int MealId { get; set; }

        public UserAccount User { get; set; } = null!;
        public Meal Meal { get; set; } = null!;
    }
}