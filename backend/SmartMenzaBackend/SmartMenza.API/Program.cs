using System;
using System.ClientModel;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Configuration;
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
                    policy.WithOrigins("http://10.0.2.2:8080", "http://localhost:8080")
                          .AllowAnyHeader()
                          .AllowAnyMethod()
                          .AllowCredentials();
                });
            });

            builder.Services.AddControllers()
                .AddJsonOptions(options =>
                {
                    options.JsonSerializerOptions.PropertyNameCaseInsensitive = true;
                });

            builder.Services.AddEndpointsApiExplorer();
            builder.Services.AddSwaggerGen();

            builder.Services.AddDbContext<SmartMenzaContext>(options =>
                options.UseSqlServer(builder.Configuration.GetConnectionString("DefaultConnection")));

            builder.Services.AddScoped<IFavoriteRepository, FavoriteRepository>();
            builder.Services.AddScoped<IGoalRepository, GoalRepository>();
            builder.Services.AddScoped<IMealRepository, MealRepository>();
            builder.Services.AddScoped<IMealTypeRepository, MealTypeRepository>();
            builder.Services.AddScoped<IMenuRepository, MenuRepository>();
            builder.Services.AddScoped<UserService>();
            builder.Services.AddScoped<MenuService>();
            builder.Services.AddScoped<GoalService>();
            builder.Services.AddScoped<FavoriteService>();
            builder.Services.AddScoped<MealService>();
            builder.Services.AddScoped<MealTypeService>();
            builder.Services.AddScoped<IRatingCommentRepository, RatingCommentRepository>();
            builder.Services.AddScoped<RatingCommentService>();
            builder.Services.AddScoped<SmartMenza.Data.Repositories.Interfaces.IStatisticsRepository, SmartMenza.Data.Repositories.Implementations.StatisticsRepository>();
            builder.Services.AddScoped<SmartMenza.Business.Services.StatisticsService>();

            builder.Services.AddSingleton(sp =>
            {
                var cfg = sp.GetRequiredService<IConfiguration>();
                const string endpoint = "https://smartmenzaai.openai.azure.com/openai/v1/";
                const string apiKey = ""; // OVDJE UBACITI API KEY
                var deploymentName = "SmartmenzaAI";

                if (string.IsNullOrWhiteSpace(endpoint))
                    throw new InvalidOperationException("AzureOpenAI:Endpoint is not configured.");
                    
                if (string.IsNullOrWhiteSpace(apiKey))
                    throw new InvalidOperationException("AzureOpenAI:ApiKey is not configured. Set AzureOpenAI:ApiKey or AZURE_OPENAI_API_KEY.");

                var client = new ChatClient(
                    credential: new AzureKeyCredential(apiKey),
                    model: deploymentName,
                    options: new OpenAIClientOptions()
                    {
                        Endpoint = new($"{endpoint}"),
                    });

                return client;
            });

            builder.Services.AddScoped<MealRecommendationService>();
            builder.Services.AddScoped<MealNutritionService>();

            var app = builder.Build();

            if (app.Environment.IsDevelopment())
            {
                app.UseDeveloperExceptionPage();
                app.UseSwagger();
                app.UseSwaggerUI();
            }

            app.UseHttpsRedirection();
            app.UseCors("AllowAndroid");
            app.UseAuthorization();

            app.MapControllers();
            app.Run();
        }
    }
}