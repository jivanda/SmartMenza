using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;

namespace SmartMenza.Domain.Entities
{
    public class UserAccount
    {
        [Key]
        public int UserId { get; set; }
        public int RoleId { get; set; }
        public string Username { get; set; } = string.Empty;
        public string Email { get; set; } = string.Empty;
        public string PasswordHash { get; set; } = string.Empty;

       public Role Role { get; set; } = null!;
    }
}