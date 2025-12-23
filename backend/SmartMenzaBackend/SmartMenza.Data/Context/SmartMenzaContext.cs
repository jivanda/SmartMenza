using Microsoft.EntityFrameworkCore;
using SmartMenza.Data.Entities;
using Microsoft.EntityFrameworkCore.Storage.ValueConversion;


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
        public DbSet<NutritionGoal> NutritionGoal { get; set; }
        public DbSet<Favorite> Favorite { get; set; }
        public DbSet<RatingComment> RatingComment { get; set; }

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            base.OnModelCreating(modelBuilder);

            var dateOnlyConverter = new ValueConverter<DateOnly, DateTime>(
                d => d.ToDateTime(TimeOnly.MinValue),
                d => DateOnly.FromDateTime(d)
            );

            modelBuilder.Entity<NutritionGoal>(entity =>
            {
                entity.ToTable("NutritionGoal");
                entity.HasKey(e => e.GoalId);

                entity.Property(e => e.DateSet)
                      .HasConversion(dateOnlyConverter)
                      .HasColumnName("DateSet");

                entity.HasOne(e => e.User)
                      .WithMany(u => u.Goals)
                      .HasForeignKey(e => e.UserId)
                      .OnDelete(DeleteBehavior.Cascade);
            });

            modelBuilder.Entity<MenuDate>(entity =>
            {
                entity.HasKey(e => new { e.MenuId, e.Date });

                entity.Property(e => e.Date)
                      .HasConversion(dateOnlyConverter);

                entity.HasOne(e => e.Menu)
                      .WithMany(m => m.MenuDates)
                      .HasForeignKey(e => e.MenuId)
                      .OnDelete(DeleteBehavior.Cascade);
            });


            modelBuilder.Entity<MenuMeal>(entity =>
            {
                entity.HasKey(e => new { e.MenuId, e.MealId });

                entity.HasOne(e => e.Menu)
                      .WithMany(m => m.MenuMeals)
                      .HasForeignKey(e => e.MenuId)
                      .OnDelete(DeleteBehavior.Cascade);

                entity.HasOne(e => e.Meal)
                      .WithMany(me => me.MenuMeals)
                      .HasForeignKey(e => e.MealId)
                      .OnDelete(DeleteBehavior.Cascade);
            });

            modelBuilder.Entity<Meal>(entity =>
            {
                entity.Property(e => e.Price).HasColumnType("decimal(10,2)");
                entity.Property(e => e.Calories).HasColumnType("decimal(10,2)");
                entity.Property(e => e.Protein).HasColumnType("decimal(10,2)");
                entity.Property(e => e.Carbohydrates).HasColumnType("decimal(10,2)");
                entity.Property(e => e.Fat).HasColumnType("decimal(10,2)");
            });

            modelBuilder.Entity<NutritionGoal>(entity =>
            {
                entity.Property(e => e.Calories).HasColumnType("decimal(10,2)");
                entity.Property(e => e.Protein).HasColumnType("decimal(10,2)");
                entity.Property(e => e.Carbohydrates).HasColumnType("decimal(10,2)");
                entity.Property(e => e.Fat).HasColumnType("decimal(10,2)");
            });

            modelBuilder.Entity<Favorite>(entity =>
            {
                entity.HasKey(f => new { f.UserId, f.MealId });

                entity.HasOne(f => f.User)
                    .WithMany()
                    .HasForeignKey(f => f.UserId)
                    .OnDelete(DeleteBehavior.Cascade);

                entity.HasOne(f => f.Meal)
                    .WithMany()
                    .HasForeignKey(f => f.MealId)
                    .OnDelete(DeleteBehavior.Cascade);
            });

            modelBuilder.Entity<RatingComment>(entity =>
            {
                entity.ToTable("RatingComment");

                entity.HasKey(rc => new { rc.UserId, rc.MealId });

                entity.Property(rc => rc.Rating).IsRequired();

                entity.HasOne(rc => rc.User)
                      .WithMany()
                      .HasForeignKey(rc => rc.UserId)
                      .OnDelete(DeleteBehavior.Cascade);

                entity.HasOne(rc => rc.Meal)
                      .WithMany(m => m.RatingComments)
                      .HasForeignKey(rc => rc.MealId)
                      .OnDelete(DeleteBehavior.Cascade);
            });
        }
    }
}