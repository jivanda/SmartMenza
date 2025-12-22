using System;
using System.Linq;
using Microsoft.EntityFrameworkCore;
using Xunit;
using SmartMenza.Data.Context;
using SmartMenza.Business.Services;
using SmartMenza.Domain.DTOs;
using SmartMenza.Data.Entities;

namespace UnitTestbackend
{
    public class RegisterUserTests
    {
        private static SmartMenzaContext CreateContext()
        {
            var options = new DbContextOptionsBuilder<SmartMenzaContext>()
                .UseInMemoryDatabase(Guid.NewGuid().ToString())
                .Options;
            return new SmartMenzaContext(options);
        }

        [Fact]
        public void RegisterUser_SucceedsAndPersistsUser()
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
            Assert.NotEqual(dto.Password, saved.PasswordHash);
            Assert.Equal(1, saved.RoleId);
        }

        [Fact]
        public void RegisterUser_ReturnsFalse_WhenEmailAlreadyExists()
        {
            using var context = CreateContext();
            var service = new UserService(context);

            var dto1 = new UserRegisterDto
            {
                Username = "user1",
                Email = "dup@example.com",
                Password = "pass1",
                RoleName = "Student"
            };

            var dto2 = new UserRegisterDto
            {
                Username = "user2",
                Email = "dup@example.com",
                Password = "pass2",
                RoleName = "Student"
            };

            var first = service.RegisterUser(dto1);
            var second = service.RegisterUser(dto2);

            Assert.True(first);
            Assert.False(second);

            var count = context.UserAccount.Count(u => u.Email == dto1.Email);
            Assert.Equal(1, count);
        }

        [Fact]
        public void RegisterUser_AssignsRoleId_ForNonStudentRoleName()
        {
            using var context = CreateContext();
            var service = new UserService(context);

            var dto = new UserRegisterDto
            {
                Username = "adminUser",
                Email = "admin@example.com",
                Password = "adminpass",
                RoleName = "Admin"
            };

            var result = service.RegisterUser(dto);

            Assert.True(result);

            var saved = context.UserAccount.FirstOrDefault(u => u.Email == dto.Email);
            Assert.NotNull(saved);
            Assert.Equal(2, saved!.RoleId);
        }

        [Fact]
        public void RegisterUser_AllowsMultipleDifferentEmails()
        {
            using var context = CreateContext();
            var service = new UserService(context);

            var dto1 = new UserRegisterDto
            {
                Username = "a",
                Email = "a@example.com",
                Password = "p1",
                RoleName = "Student"
            };

            var dto2 = new UserRegisterDto
            {
                Username = "b",
                Email = "b@example.com",
                Password = "p2",
                RoleName = "Admin"
            };

            Assert.True(service.RegisterUser(dto1));
            Assert.True(service.RegisterUser(dto2));

            var total = context.UserAccount.Count();
            Assert.Equal(2, total);
        }
    }
}