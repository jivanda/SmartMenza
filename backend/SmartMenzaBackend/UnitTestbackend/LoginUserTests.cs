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
    public static class LoginTestContext
    {
        private static SmartMenzaContext CreateContext()
        {
            var options = new DbContextOptionsBuilder<SmartMenzaContext>()
                .UseInMemoryDatabase(Guid.NewGuid().ToString())
                .Options;
            return new SmartMenzaContext(options);
        }
    }
}