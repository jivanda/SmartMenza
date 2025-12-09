package com.example.smartmenza.data.remote

data class MealDto(
    val mealId: Int,
    val name: String,
    val description: String?,
    val price: Double,
    val calories: Double?,
    val protein: Double?,
    val carbohydrates: Double?,
    val fat: Double?
)

data class MenuResponseDto(
    val menuId: Int,
    val name: String,
    val description: String?,
    val date: String,
    val menuTypeName: String?,
    val meals: List<MealDto>
)

data class FavoriteMealDto(
    val mealId: Int,
    val mealName: String,
    val calories: Double,
    val protein: Double,
    val imageUrl: String?
)

data class FavoriteToggleDto(
    val mealId: Int
)

data class FavoriteStatusDto(
    val isFavorite: Boolean
)
