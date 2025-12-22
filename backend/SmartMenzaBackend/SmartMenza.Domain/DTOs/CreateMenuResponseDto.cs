using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SmartMenza.Domain.DTOs
{
    public class CreateMenuResponseDto
    {
        public string Message { get; set; } = string.Empty;
        public int MenuId { get; set; }
    }
}