using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SmartMenza.Domain.DTOs
{
    public class UpdateMenuDto
    {
        public string Name { get; set; } = string.Empty;
        public string? Description { get; set; }
        public int MenuTypeId { get; set; }
        public List<MenuMealItemDto> Meals { get; set; } = new();
    }
}