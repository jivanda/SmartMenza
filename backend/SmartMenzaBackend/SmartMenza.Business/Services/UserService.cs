using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using SmartMenza.Data.Context;
using SmartMenza.Domain.DTOs;
using SmartMenza.Domain.Entities;
using System.Security.Cryptography;

namespace SmartMenza.Business.Services
{
    public class UserService
    {
        private readonly SmartMenzaContext _context;

        public UserService(SmartMenzaContext context)
        {
            _context = context;
        }

        public bool Registriraj(UserRegisterDto dto)
        {
            if (_context.Users.Any(u => u.Email == dto.Email))
                return false;

            var user = new User
            {
                Ime = dto.Ime,
                Email = dto.Email,
                LozinkaHash = HashirajLozinku(dto.Lozinka),
                Uloga = dto.Uloga
            };

            _context.Users.Add(user);
            _context.SaveChanges();
            return true;
        }

        public User? Prijavi(UserLoginDto dto)
        {
            var user = _context.Users.FirstOrDefault(u => u.Email == dto.Email);
            if (user == null) return null;

            var hash = HashirajLozinku(dto.Lozinka);
            if (user.LozinkaHash != hash) return null;

            return user;
        }

        private string HashirajLozinku(string lozinka)
        {
            using var sha = SHA256.Create();
            var bytes = Encoding.UTF8.GetBytes(lozinka);
            var hash = sha.ComputeHash(bytes);
            return Convert.ToBase64String(hash);
        }
    }
}