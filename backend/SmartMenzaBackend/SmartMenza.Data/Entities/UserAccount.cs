using System.ComponentModel.DataAnnotations;
using System.Collections.Generic;
using SmartMenza.Data.Entities;

namespace SmartMenza.Data.Entities
{
    public class UserAccount
    {
        [Key]
        public int UserId { get; set; }
        public int RoleId { get; set; }
        public string Username { get; set; }
        public string Email { get; set; }
        public string? GoogleId { get; set; }
        public string PasswordHash { get; set; }
        public Role Role { get; set; }
        public List<NutritionGoal> Goals { get; set; } = new();
    }
}