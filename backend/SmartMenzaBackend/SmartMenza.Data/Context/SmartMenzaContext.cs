using Microsoft.EntityFrameworkCore;
using SmartMenza.Domain.Entities;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SmartMenza.Data.Context
{
    namespace SmartMenza.Data.Context
    {
        public class SmartMenzaContext : DbContext
        {
            public SmartMenzaContext(DbContextOptions<SmartMenzaContext> options) : base(options) { }
            public DbSet<UserAccount> UserAccount { get; set; }
            public DbSet<Role> Role { get; set; }
            public DbSet<Menu> Menu { get; set; }
            public DbSet<MenuType> MenuType { get; set; }
            public DbSet<MenuDate> MenuDate { get; set; }
            public DbSet<Meal> Meal { get; set; }
            public DbSet<MealType> MealType { get; set; }
            public DbSet<MenuMeal> MenuMeal { get; set; }

            protected override void OnModelCreating(ModelBuilder modelBuilder)
            {
                base.OnModelCreating(modelBuilder);

                modelBuilder.Entity<MenuDate>()
                    .HasKey(md => new { md.MenuId, md.Date });

                modelBuilder.Entity<MenuDate>()
                    .HasOne(md => md.Menu)
                    .WithMany(m => m.MenuDates)
                    .HasForeignKey(md => md.MenuId);

                modelBuilder.Entity<MenuMeal>()
                    .HasKey(mm => new { mm.MenuId, mm.MealId });

                modelBuilder.Entity<MenuMeal>()
                    .HasOne(mm => mm.Menu)
                    .WithMany(m => m.MenuMeals)
                    .HasForeignKey(mm => mm.MenuId);

                modelBuilder.Entity<MenuMeal>()
                    .HasOne(mm => mm.Meal)
                    .WithMany(m => m.MenuMeals)
                    .HasForeignKey(mm => mm.MealId);
            }
        }
    }
}