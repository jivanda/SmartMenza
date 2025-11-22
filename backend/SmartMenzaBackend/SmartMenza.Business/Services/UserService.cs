using Microsoft.EntityFrameworkCore;
using SmartMenza.Data.Context;
using SmartMenza.Domain.DTOs;
using SmartMenza.Domain.Entities;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Security.Cryptography;
using System.Text;
using System.Threading.Tasks;

namespace SmartMenza.Business.Services
{
    public class UserService
    {
        private readonly SmartMenzaContext _context;

        public UserService(SmartMenzaContext context)
        {
            _context = context;
        }

        public bool RegisterUser(UserRegisterDto dto)
        {
            if (_context.UserAccount.Any(u => u.Email == dto.Email))
                return false;

            var user = new UserAccount
            {
                Username = dto.Username,
                Email = dto.Email,
                PasswordHash = HashPassword(dto.Password),
                RoleId = dto.RoleName == "Student" ? 1 : 2
            };

            _context.UserAccount.Add(user);
            _context.SaveChanges();
            return true;
        }

        public UserAccount? LoginUser(UserLoginDto dto)
        {
            var user = _context.UserAccount.Include(u => u.Role).FirstOrDefault(u => u.Email == dto.Email);
            if (user == null) return null;

            var hash = HashPassword(dto.Password);
            if (user.PasswordHash != hash) return null;

            return user;
        }

        public string HashPassword(string lozinka)
        {
            using var sha = SHA256.Create();
            var bytes = Encoding.UTF8.GetBytes(lozinka);
            var hash = sha.ComputeHash(bytes);
            return Convert.ToBase64String(hash);
        }
    }
}