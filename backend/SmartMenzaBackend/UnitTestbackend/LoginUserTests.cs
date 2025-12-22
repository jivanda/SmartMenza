using System;
using System.Linq;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.InMemory;
using Xunit;
using SmartMenza.Data.Context;
using SmartMenza.Business.Services;
using SmartMenza.Domain.DTOs;
using SmartMenza.Data.Entities;

namespace UnitTestbackend
{
    public class LoginUserTests
    {
        public static SmartMenzaContext CreateContext()
        {
            var options = new DbContextOptionsBuilder<SmartMenzaContext>()
                .UseInMemoryDatabase(Guid.NewGuid().ToString())
                .Options;
            return new SmartMenzaContext(options);
        }

        private static void SeedRole(SmartMenzaContext context, int roleId = 1, string roleName = "Student")
        {
            if (!context.Role.Any(r => r.RoleId == roleId))
            {
                context.Role.Add(new Role { RoleId = roleId, RoleName = roleName });
                context.SaveChanges();
            }
        }

        private static void SeedUserViaService(SmartMenzaContext context, UserRegisterDto dto)
        {
            var roleId = dto.RoleName == "Student" ? 1 : 2;
            var roleName = dto.RoleName;
            SeedRole(context, roleId, roleName);

            var service = new UserService(context);
            var ok = service.RegisterUser(dto);
            if (!ok)
                throw new InvalidOperationException("Failed to seed user: email already exists.");
        }

        [Fact]
        public void LoginUser_ReturnsUser_WhenCredentialsAreValid()
        {
            using var context = CreateContext();

            var registerDto = new UserRegisterDto
            {
                Username = "tester",
                Email = "tester@example.com",
                Password = "MySecret123!",
                RoleName = "Student"
            };
            SeedUserViaService(context, registerDto);

            var service = new UserService(context);
            var loginDto = new UserLoginDto { Email = registerDto.Email, Password = registerDto.Password };

            var user = service.LoginUser(loginDto);

            Assert.NotNull(user);
            Assert.Equal(registerDto.Username, user!.Username);
            Assert.Equal(registerDto.Email, user.Email);
            Assert.Equal(1, user.RoleId);
            Assert.NotNull(user.Role);
            Assert.Equal("Student", user.Role.RoleName);
        }

        [Fact]
        public void LoginUser_ReturnsNull_WhenEmailDoesNotExist()
        {
            using var context = CreateContext();

            var service = new UserService(context);
            var loginDto = new UserLoginDto { Email = "noone@example.com", Password = "whatever" };

            var user = service.LoginUser(loginDto);

            Assert.Null(user);
        }

        [Fact]
        public void LoginUser_ReturnsNull_WhenPasswordIsIncorrect()
        {
            using var context = CreateContext();

            var registerDto = new UserRegisterDto
            {
                Username = "tester2",
                Email = "tester2@example.com",
                Password = "CorrectPassword!",
                RoleName = "Student"
            };
            SeedUserViaService(context, registerDto);

            var service = new UserService(context);
            var loginDto = new UserLoginDto { Email = registerDto.Email, Password = "WrongPassword!" };

            var user = service.LoginUser(loginDto);

            Assert.Null(user);
        }

        [Fact]
        public void LoginUser_IsCaseSensitive_ForEmail_InInMemoryProvider()
        {
            using var context = CreateContext();

            var registerDto = new UserRegisterDto
            {
                Username = "caseUser",
                Email = "CaseUser@example.com",
                Password = "Pwd123!",
                RoleName = "Student"
            };
            SeedUserViaService(context, registerDto);

            var service = new UserService(context);

            var loginDto = new UserLoginDto { Email = "caseuser@example.com", Password = registerDto.Password };
            var user = service.LoginUser(loginDto);

            Assert.Null(user);
        }

        [Fact]
        public void LoginUser_LoadsRoleNavigation_WhenRoleExists()
        {
            using var context = CreateContext();

            SeedRole(context, 2, "Admin");

            var registerDto = new UserRegisterDto
            {
                Username = "admin",
                Email = "admin@example.com",
                Password = "AdminPass!",
                RoleName = "Admin"
            };
            SeedUserViaService(context, registerDto);

            var service = new UserService(context);
            var loginDto = new UserLoginDto { Email = registerDto.Email, Password = registerDto.Password };

            var user = service.LoginUser(loginDto);

            Assert.NotNull(user);
            Assert.Equal(2, user!.RoleId);
            Assert.NotNull(user.Role);
            Assert.Equal("Admin", user.Role.RoleName);
        }
    }
}