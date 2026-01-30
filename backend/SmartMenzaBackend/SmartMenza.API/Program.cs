using System;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using SmartMenza.Business.Services;
using SmartMenza.Data.Context;
using SmartMenza.Data.Repositories.Interfaces;
using SmartMenza.Data.Repositories.Implementations;
using OpenAI;
using OpenAI.Chat;
using Azure;

namespace SmartMenza.API
{
    public class Program
    {
        public static void Main(string[] args)
        {
            var builder = WebApplication.CreateBuilder(args);


            builder.Services.AddCors(options =>
            {
                options.AddPolicy("AllowAndroid", policy =>
                {
                    policy.WithOrigins(
                            "http://10.0.2.2:8080",
                            "http://localhost:8080"
                        )
                        .AllowAnyHeader()
                        .AllowAnyMethod()
                        .AllowCredentials();
                });
            });

            builder.Services.AddControllers()
                .AddJsonOptions(options =>
                {
                    options.JsonSerializerOptions.PropertyNameCaseInsensitive = true;
                    options.JsonSerializerOptions.PropertyNamingPolicy = System.Text.Json.JsonNamingPolicy.CamelCase;
                    options.JsonSerializerOptions.DictionaryKeyPolicy = System.Text.Json.JsonNamingPolicy.CamelCase;
                });

            builder.Services.AddEndpointsApiExplorer();
            builder.Services.AddSwaggerGen();

            var connectionString = builder.Configuration.GetConnectionString("DefaultConnection");

            if (string.IsNullOrWhiteSpace(connectionString))
                throw new InvalidOperationException("ConnectionStrings:DefaultConnection is not configured.");

            builder.Services.AddDbContext<SmartMenzaContext>(options =>
                options.UseSqlServer(connectionString));

            builder.Services.AddScoped<IFavoriteRepository, FavoriteRepository>();
            builder.Services.AddScoped<IGoalRepository, GoalRepository>();
            builder.Services.AddScoped<IMealRepository, MealRepository>();
            builder.Services.AddScoped<IMealTypeRepository, MealTypeRepository>();
            builder.Services.AddScoped<IMenuRepository, MenuRepository>();
            builder.Services.AddScoped<IRatingCommentRepository, RatingCommentRepository>();
            builder.Services.AddScoped<IStatisticsRepository, StatisticsRepository>();

            builder.Services.AddScoped<UserService>();
            builder.Services.AddScoped<MenuService>();
            builder.Services.AddScoped<GoalService>();
            builder.Services.AddScoped<FavoriteService>();
            builder.Services.AddScoped<MealService>();
            builder.Services.AddScoped<MealTypeService>();
            builder.Services.AddScoped<RatingCommentService>();
            builder.Services.AddScoped<StatisticsService>();
            builder.Services.AddScoped<MealRecommendationService>();
            builder.Services.AddScoped<MealNutritionService>();

            builder.Services.AddSingleton(sp =>
            {
                var config = sp.GetRequiredService<IConfiguration>();

                var endpoint = config["AzureOpenAI:Endpoint"];
                var apiKey = config["AzureOpenAI:ApiKey"];
                var deploymentName = config["AzureOpenAI:DeploymentName"];

                if (string.IsNullOrWhiteSpace(endpoint))
                    throw new InvalidOperationException("AzureOpenAI:Endpoint is not configured.");

                if (string.IsNullOrWhiteSpace(apiKey))
                    throw new InvalidOperationException("AzureOpenAI:ApiKey is not configured.");

                if (string.IsNullOrWhiteSpace(deploymentName))
                    throw new InvalidOperationException("AzureOpenAI:DeploymentName is not configured.");

                return new ChatClient(
                    credential: new AzureKeyCredential(apiKey),
                    model: deploymentName,
                    options: new OpenAIClientOptions
                    {
                        Endpoint = new Uri(endpoint)
                    });
            });

            var app = builder.Build();

            if (app.Environment.IsDevelopment())
            {
                app.UseDeveloperExceptionPage();
            }

            app.UseSwagger();
            app.UseSwaggerUI();

            app.MapGet("/", () => Results.Redirect("/swagger"));

            if (!app.Environment.IsDevelopment())
            {
                app.UseHttpsRedirection();
            }

            app.UseStaticFiles();
            app.UseCors("AllowAndroid");
            app.UseAuthorization();

            app.MapControllers();
            app.Run();
        }
    }
}
