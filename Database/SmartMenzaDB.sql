-------------------------------------------------------
-- Create SmartMenzaDB (if not exists)
-------------------------------------------------------
IF DB_ID('SmartMenzaDB') IS NULL
    CREATE DATABASE SmartMenzaDB;
GO

USE SmartMenzaDB;
GO

-------------------------------------------------------
-- Drop existing tables for re-runs (safe order)
-------------------------------------------------------
IF OBJECT_ID('dbo.MenuDate', 'U') IS NOT NULL DROP TABLE dbo.MenuDate;
IF OBJECT_ID('dbo.RatingComment', 'U') IS NOT NULL DROP TABLE dbo.RatingComment;
IF OBJECT_ID('dbo.Favorite', 'U') IS NOT NULL DROP TABLE dbo.Favorite;
IF OBJECT_ID('dbo.NutritionGoal', 'U') IS NOT NULL DROP TABLE dbo.NutritionGoal;
IF OBJECT_ID('dbo.MenuMeal', 'U') IS NOT NULL DROP TABLE dbo.MenuMeal;
IF OBJECT_ID('dbo.Menu', 'U') IS NOT NULL DROP TABLE dbo.Menu;
IF OBJECT_ID('dbo.Meal', 'U') IS NOT NULL DROP TABLE dbo.Meal;
IF OBJECT_ID('dbo.MealType', 'U') IS NOT NULL DROP TABLE dbo.MealType;
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
-- Table: MealType
-------------------------------------------------------
CREATE TABLE MealType (
    MealTypeId  INT IDENTITY(1,1) PRIMARY KEY,
    Name NVARCHAR(100) NOT NULL
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
    MealTypeId INT NOT NULL,
    Calories DECIMAL(6,2),
    Protein DECIMAL(6,2),
    Carbohydrates DECIMAL(6,2),
    Fat DECIMAL(6,2),
    ImageUrl NVARCHAR(255),
    CONSTRAINT FK_MealType
        FOREIGN KEY (MealTypeId)
        REFERENCES MealType(MealTypeId)
        ON DELETE NO ACTION
        ON UPDATE CASCADE
);
GO

-------------------------------------------------------
-- Table: Menu
-------------------------------------------------------
CREATE TABLE Menu (
    MenuId INT IDENTITY(1,1) PRIMARY KEY,
    Name NVARCHAR(100) NOT NULL,
    Description NVARCHAR(255)
);
GO

-------------------------------------------------------
-- Table: MenuDate
-------------------------------------------------------
CREATE TABLE MenuDate (
    MenuId INT NOT NULL,
    Date DATE NOT NULL,
    CONSTRAINT PK_MenuDate PRIMARY KEY (MenuId, Date),
    CONSTRAINT FK_MenuDate_Menu FOREIGN KEY (MenuId) REFERENCES Menu(MenuId) ON DELETE CASCADE
);
GO

-------------------------------------------------------
-- Table: MenuMeal (many-to-many link)
-------------------------------------------------------
CREATE TABLE MenuMeal (
    MenuId INT NOT NULL,
    MealId INT NOT NULL,
    CONSTRAINT PK_MenuMeal PRIMARY KEY (MenuId, MealId),
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
    DateSet DATE DEFAULT CAST(GETDATE() AS DATE),
    CONSTRAINT FK_Goal_User FOREIGN KEY (UserId)
        REFERENCES UserAccount(UserId)
        ON DELETE CASCADE
);
GO

-------------------------------------------------------
-- Table: Favorite
-------------------------------------------------------
CREATE TABLE Favorite (
    UserId INT NOT NULL,
    MealId INT NOT NULL,
    CONSTRAINT PK_Favorite PRIMARY KEY (UserId, MealId),
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
-- Seed Data (in dependency-safe order)
-------------------------------------------------------

-- Roles (seed first so UserAccount FK succeeds)
INSERT INTO Role (RoleName)
VALUES ('Student'), ('Employee');
GO

-- Users (RoleId references Role table)
INSERT INTO UserAccount (RoleId, Username, Email, PasswordHash)
VALUES 
    (1, 'mark.student', 'mark@student.com', 'lozinka123'),
    (2, 'ivan.employee', 'ivan@employee.com', 'sigurnalozinka');
GO

-- Meal types (must exist before inserting Meals)
INSERT INTO MealType (Name)
VALUES ('Soup'), ('Main meal'), ('Salad');
GO

-- Meals (full rows — MealTypeId values should match inserted MealType rows)
INSERT INTO Meal (Name, Description, Price, MealTypeId, Calories, Protein, Carbohydrates, Fat, ImageUrl)
VALUES 
    ('Chicken with Rice', 'Tasty chicken with vegetables and rice', 25.50, 2, 550, 35, 60, 15, 'chicken_rice.jpg'),
    ('Vegetarian Salad', 'Mixed salad with tofu', 18.00, 3, 300, 15, 25, 10, 'salad.jpg'),
    ('Beef Steak', 'Juicy premium steak', 45.00, 2, 700, 50, 10, 40, 'steak.jpg'),
    ('Pasta Carbonara', 'Creamy pasta with pancetta', 30.00, 2, 600, 20, 75, 25, 'carbonara.jpg');
GO

-- Menus
INSERT INTO Menu (Name, Description)
VALUES 
    ('Weekly Menu 1', 'Best selection for the start of the week'),
    ('Weekly Menu 2', 'Diverse meals for midweek');
GO

-- MenuDate (link menus to specific dates)
INSERT INTO MenuDate (MenuId, Date)
VALUES 
    (1, '2025-11-03'),
    (2, '2025-11-04');
GO

-- MenuMeal linking (MenuId, MealId) - ensure MealId values exist
INSERT INTO MenuMeal (MenuId, MealId)
VALUES (1, 1), (1, 2), (2, 3), (2, 4);
GO

-- Nutrition goals
INSERT INTO NutritionGoal (UserId, Calories, Protein, Carbohydrates, Fat)
VALUES (1, 2000, 100, 250, 70), (2, 2500, 120, 300, 80);
GO

-- Favorites (UserId, MealId) - must exist in UserAccount and Meal
INSERT INTO Favorite (UserId, MealId)
VALUES (1, 1), (1, 2), (2, 3), (2, 4);
GO

-- Ratings & comments
INSERT INTO RatingComment (UserId, MealId, Rating, Comment)
VALUES
    (1, 1, 5, 'Excellent preparation!'),
    (1, 2, 4, 'Very good salad, could use a bit more spice.'),
    (2, 3, 5, 'Perfectly cooked!'),
    (2, 4, 3, 'Okay, but could be creamier.');
GO

-------------------------------------------------------
-- End of SmartMenzaDB.sql (corrected)
-------------------------------------------------------
