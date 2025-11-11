using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SmartMenza.Domain.DTOs
{
    public class UserLoginDto
    {
        public string Email { get; set; } = string.Empty;
        public string Lozinka { get; set; } = string.Empty;
    }
}