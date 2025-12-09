USE SmartMenzaDB;
GO

-------------------------------------------------------
-- Očisti stare auto-generirane menije
-------------------------------------------------------
DELETE md
FROM MenuDate md
JOIN Menu m ON m.MenuId = md.MenuId
WHERE m.Name LIKE 'Daily menu %';

DELETE FROM Menu
WHERE Name LIKE 'Daily menu %';
GO

-------------------------------------------------------
-- Parametri raspona datuma
-------------------------------------------------------
DECLARE @startDate DATE = '2025-01-01';   -- OVDJE PROMIJENI POČETNI DATUM AKO TREBA
DECLARE @endDate   DATE = '2026-12-31';   -- OVDJE PROMIJENI ZAVRŠNI DATUM AKO TREBA

-------------------------------------------------------
-- 1. Generiraj sve datume u #Dates
-------------------------------------------------------
IF OBJECT_ID('tempdb..#Dates') IS NOT NULL DROP TABLE #Dates;

CREATE TABLE #Dates
(
    DateId INT IDENTITY(1,1) PRIMARY KEY,
    [Date] DATE NOT NULL
);

;WITH Dates AS (
    SELECT @startDate AS [Date]
    UNION ALL
    SELECT DATEADD(DAY, 1, [Date])
    FROM Dates
    WHERE [Date] < @endDate
)
INSERT INTO #Dates([Date])
SELECT [Date]
FROM Dates
OPTION (MAXRECURSION 0);

-------------------------------------------------------
-- 2. Insert u Menu i uhvati nove MenuId u tablicu varijablu
-------------------------------------------------------
DECLARE @InsertedMenus TABLE
(
    RowNum INT IDENTITY(1,1) PRIMARY KEY,
    MenuId INT NOT NULL
);

INSERT INTO Menu (MenuTypeId, Name, Description)
OUTPUT inserted.MenuId INTO @InsertedMenus(MenuId)
SELECT
    CASE 
        WHEN MONTH(d.[Date]) BETWEEN 9 AND 11 THEN 1   -- jesen -> Weekly Menu 1 Fall
        WHEN MONTH(d.[Date]) BETWEEN 3 AND 5  THEN 2   -- proljeće -> Weekly Menu 2 Spring
        ELSE 3                                         -- ostalo -> Vegetarian Menu Winter
    END AS MenuTypeId,
    'Daily menu ' + CONVERT(varchar(10), d.[Date], 23),          -- npr. Daily menu 2025-01-01
    'Auto-generated menu for ' + CONVERT(varchar(10), d.[Date], 104) -- npr. 01.01.2025
FROM #Dates d
ORDER BY d.[Date];

-------------------------------------------------------
-- 3. Upari nove MenuId s datumima i popuni MenuDate
-------------------------------------------------------
INSERT INTO MenuDate (MenuId, [Date])
SELECT im.MenuId, d.[Date]
FROM @InsertedMenus im
JOIN #Dates d
    ON d.DateId = im.RowNum;

-------------------------------------------------------
-- 4. Brza provjera
-------------------------------------------------------
SELECT TOP 10 * 
FROM Menu 
WHERE Name LIKE 'Daily menu %'
ORDER BY MenuId;

SELECT TOP 10 *
FROM MenuDate
ORDER BY [Date];
GO