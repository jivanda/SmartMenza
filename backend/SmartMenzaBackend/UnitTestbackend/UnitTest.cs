using System;
using System.Linq;
using Microsoft.EntityFrameworkCore;
using Xunit;
using SmartMenza.Data.Context;
using SmartMenza.Business.Services;
using SmartMenza.Domain.DTOs;
using SmartMenza.Domain.Entities;

namespace UnitTestbackend
{
    public class RegistrationTests
    {
        private static SmartMenzaContext CreateContext()
        {
            var options = new DbContextOptionsBuilder<SmartMenzaContext>()
                .UseInMemoryDatabase(Guid.NewGuid().ToString())
                .Options;
            return new SmartMenzaContext(options);
        }

        [Fact]
        public void RegisterUser_Succeeds()
        {
            using var context = CreateContext();

            var service = new UserService(context);

            var dto = new UserRegisterDto
            {
                Username = "tester",
                Email = "tester@example.com",
                Password = "MySecret123!",
                RoleName = "Student"
            };

            var result = service.RegisterUser(dto);

            Assert.True(result);

            var saved = context.UserAccount.FirstOrDefault(u => u.Email == dto.Email);
            Assert.NotNull(saved);
            Assert.Equal(dto.Username, saved!.Username);
            Assert.False(string.IsNullOrEmpty(saved.PasswordHash));
            Assert.Equal(1, saved.RoleId);
        }
    }
}