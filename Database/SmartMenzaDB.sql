-------------------------------------------------------
-- Create SmartMenzaDB (if not exists)
-------------------------------------------------------
IF DB_ID('SmartMenzaDB') IS NULL
    CREATE DATABASE SmartMenzaDB;
GO

USE SmartMenzaDB;
GO

-------------------------------------------------------
-- Drop existing tables for re-runs
-------------------------------------------------------
IF OBJECT_ID('dbo.RatingComment', 'U') IS NOT NULL DROP TABLE dbo.RatingComment;
IF OBJECT_ID('dbo.Favorite', 'U') IS NOT NULL DROP TABLE dbo.Favorite;
IF OBJECT_ID('dbo.NutritionGoal', 'U') IS NOT NULL DROP TABLE dbo.NutritionGoal;
IF OBJECT_ID('dbo.MenuMeal', 'U') IS NOT NULL DROP TABLE dbo.MenuMeal;
IF OBJECT_ID('dbo.Menu', 'U') IS NOT NULL DROP TABLE dbo.Menu;
IF OBJECT_ID('dbo.Meal', 'U') IS NOT NULL DROP TABLE dbo.Meal;
IF OBJECT_ID('dbo.UserAccount', 'U') IS NOT NULL DROP TABLE dbo.UserAccount;
IF OBJECT_ID('dbo.Role', 'U') IS NOT NULL DROP TABLE dbo.Role;
GO

-------------------------------------------------------
-- Table: Role
-------------------------------------------------------
CREATE TABLE Role (
    RoleId INT IDENTITY(1,1) PRIMARY KEY,
    RoleName NVARCHAR(50) NOT NULL
);
GO

-------------------------------------------------------
-- Table: UserAccount
-------------------------------------------------------
CREATE TABLE UserAccount (
    UserId INT IDENTITY(1,1) PRIMARY KEY,
    RoleId INT NOT NULL,
    Username NVARCHAR(50) NOT NULL,
    Email NVARCHAR(50) NOT NULL,
    PasswordHash NVARCHAR(255) NOT NULL,
    CONSTRAINT FK_User_Role
        FOREIGN KEY (RoleId)
        REFERENCES Role(RoleId)
        ON DELETE NO ACTION
        ON UPDATE CASCADE
);
GO

-------------------------------------------------------
-- Table: Meal
-------------------------------------------------------
CREATE TABLE Meal (
    MealId INT IDENTITY(1,1) PRIMARY KEY,
    Name NVARCHAR(100) NOT NULL,
    Description NVARCHAR(255),
    Price DECIMAL(6,2),
    Calories DECIMAL(6,2),
    Protein DECIMAL(6,2),
    Carbohydrates DECIMAL(6,2),
    Fat DECIMAL(6,2),
    ImageUrl NVARCHAR(255)
);
GO

-------------------------------------------------------
-- Table: Menu
-------------------------------------------------------
CREATE TABLE Menu (
    MenuId INT IDENTITY(1,1) PRIMARY KEY,
    Name NVARCHAR(100) NOT NULL,
    Description NVARCHAR(255),
    Date DATE NOT NULL
);
GO

-------------------------------------------------------
-- Table: MenuMeal (many-to-many link)
-------------------------------------------------------
CREATE TABLE MenuMeal (
    MenuMealId INT IDENTITY(1,1) PRIMARY KEY,
    MenuId INT NOT NULL,
    MealId INT NOT NULL,
    CONSTRAINT FK_MenuMeal_Menu FOREIGN KEY (MenuId) REFERENCES Menu(MenuId) ON DELETE CASCADE,
    CONSTRAINT FK_MenuMeal_Meal FOREIGN KEY (MealId) REFERENCES Meal(MealId) ON DELETE CASCADE
);
GO

-------------------------------------------------------
-- Table: NutritionGoal
-------------------------------------------------------
CREATE TABLE NutritionGoal (
    GoalId INT IDENTITY(1,1) PRIMARY KEY,
    UserId INT NOT NULL,
    Calories DECIMAL(6,2),
    Protein DECIMAL(6,2),
    Carbohydrates DECIMAL(6,2),
    Fat DECIMAL(6,2),
    DateSet DATE DEFAULT GETDATE(),
    CONSTRAINT FK_Goal_User FOREIGN KEY (UserId)
        REFERENCES UserAccount(UserId)
        ON DELETE CASCADE
);
GO

-------------------------------------------------------
-- Table: Favorite
-------------------------------------------------------
CREATE TABLE Favorite (
    FavoriteId INT IDENTITY(1,1) PRIMARY KEY,
    UserId INT NOT NULL,
    MealId INT NOT NULL,
    CONSTRAINT FK_Favorite_User FOREIGN KEY (UserId)
        REFERENCES UserAccount(UserId)
        ON DELETE CASCADE,
    CONSTRAINT FK_Favorite_Meal FOREIGN KEY (MealId)
        REFERENCES Meal(MealId)
        ON DELETE CASCADE
);
GO

-------------------------------------------------------
-- Table: RatingComment
-------------------------------------------------------
CREATE TABLE RatingComment (
    RatingId INT IDENTITY(1,1) PRIMARY KEY,
    UserId INT NOT NULL,
    MealId INT NOT NULL,
    Rating INT CHECK (Rating BETWEEN 1 AND 5),
    Comment NVARCHAR(255),
    Date DATETIME DEFAULT GETDATE(),
    CONSTRAINT FK_Rating_User FOREIGN KEY (UserId)
        REFERENCES UserAccount(UserId)
        ON DELETE CASCADE,
    CONSTRAINT FK_Rating_Meal FOREIGN KEY (MealId)
        REFERENCES Meal(MealId)
        ON DELETE CASCADE
);
GO

-------------------------------------------------------
-- Seed Data
-------------------------------------------------------
INSERT INTO Role (RoleName)
VALUES ('Student'), ('Employee');
GO

INSERT INTO UserAccount (RoleId, Username, Email, PasswordHash)
VALUES 
    (1, 'mark.student', 'mark@student.com', 'lozinka123'),
    (2, 'ivan.employee', 'ivan@employee.com', 'sigurnalozinka');
GO

INSERT INTO Meal (Name, Description, Price, Calories, Protein, Carbohydrates, Fat, ImageUrl)
VALUES 
('Chicken with Rice', 'Tasty chicken with vegetables and rice', 25.50, 550, 35, 60, 15, 'chicken_rice.jpg'),
('Vegetarian Salad', 'Mixed salad with tofu', 18.00, 300, 15, 25, 10, 'salad.jpg'),
('Beef Steak', 'Juicy premium steak', 45.00, 700, 50, 10, 40, 'steak.jpg'),
('Pasta Carbonara', 'Creamy pasta with pancetta', 30.00, 600, 20, 75, 25, 'carbonara.jpg');
GO

INSERT INTO Menu (Name, Description, Date)
VALUES 
('Weekly Menu 1', 'Best selection for the start of the week', '2025-11-03'),
('Weekly Menu 2', 'Diverse meals for midweek', '2025-11-04');
GO

INSERT INTO MenuMeal (MenuId, MealId)
VALUES (1, 1), (1, 2), (2, 3), (2, 4);
GO

INSERT INTO NutritionGoal (UserId, Calories, Protein, Carbohydrates, Fat)
VALUES (1, 2000, 100, 250, 70), (2, 2500, 120, 300, 80);
GO

INSERT INTO Favorite (UserId, MealId)
VALUES (1, 1), (1, 2), (2, 3), (2, 4);
GO

INSERT INTO RatingComment (UserId, MealId, Rating, Comment)
VALUES
(1, 1, 5, 'Excellent preparation!'),
(1, 2, 4, 'Very good salad, could use a bit more spice.'),
(2, 3, 5, 'Perfectly cooked!'),
(2, 4, 3, 'Okay, but could be creamier.');
GO
-------------------------------------------------------
-- End of SmartMenzaDB.sql