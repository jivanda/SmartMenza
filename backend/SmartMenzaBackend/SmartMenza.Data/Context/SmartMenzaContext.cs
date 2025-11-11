using Microsoft.EntityFrameworkCore;
using SmartMenza.Domain.Entities;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SmartMenza.Data.Context
{
    public class SmartMenzaContext : DbContext
    {
        public SmartMenzaContext(DbContextOptions<SmartMenzaContext> options) : base(options) { }

        public DbSet<User> Users { get; set; }
    }
}