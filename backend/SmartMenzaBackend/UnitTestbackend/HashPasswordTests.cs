using System;
using Microsoft.EntityFrameworkCore;
using Xunit;
using SmartMenza.Business.Services;
using SmartMenza.Data.Context;

namespace UnitTestbackend
{
    public class HashPasswordTests
    {
        // Isto kao u RegisterUserTests – koristimo in-memory bazu
        private static SmartMenzaContext CreateContext()
        {
            var options = new DbContextOptionsBuilder<SmartMenzaContext>()
                .UseInMemoryDatabase(Guid.NewGuid().ToString())
                .Options;

            return new SmartMenzaContext(options);
        }

        private readonly UserService _sut; // System Under Test

        public HashPasswordTests()
        {
            var context = CreateContext();
            _sut = new UserService(context);
        }

        [Fact]
        public void HashPassword_ReturnsNonEmptyString()
        {
            // Arrange
            var password = "MojaTajna123!";

            // Act
            var hash = _sut.HashPassword(password);

            // Assert
            Assert.False(string.IsNullOrWhiteSpace(hash));
        }

        [Fact]
        public void HashPassword_DoesNotReturnOriginalPassword()
        {
            var password = "MojaTajna123!";

            var hash = _sut.HashPassword(password);

            Assert.NotEqual(password, hash);
        }

        [Fact]
        public void HashPassword_SameInput_ReturnsSameHash()
        {
            var password = "IstaLozinka";

            var hash1 = _sut.HashPassword(password);
            var hash2 = _sut.HashPassword(password);

            Assert.Equal(hash1, hash2);
        }

        [Fact]
        public void HashPassword_DifferentInputs_ReturnDifferentHashes()
        {
            var password1 = "Lozinka123";
            var password2 = "Lozinka456";

            var hash1 = _sut.HashPassword(password1);
            var hash2 = _sut.HashPassword(password2);

            Assert.NotEqual(hash1, hash2);
        }

        [Fact]
        public void HashPassword_NullInput_ThrowsArgumentNullException()
        {
            string? password = null;

            Assert.Throws<ArgumentNullException>(() => _sut.HashPassword(password));
        }
    }
}

