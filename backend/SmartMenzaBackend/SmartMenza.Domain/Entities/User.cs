using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SmartMenza.Domain.Entities
{
    public class User
    {
        public int Id { get; set; }
        public string Ime { get; set; } = string.Empty;
        public string Email { get; set; } = string.Empty;
        public string LozinkaHash { get; set; } = string.Empty;
        public string Uloga { get; set; } = "Student"; // ili "Zaposlenik"
    }
}
