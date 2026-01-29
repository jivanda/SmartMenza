package com.example.smartmenza.data.remote

import MealDto

data class MenuResponseDto(
    val menuId: Int,
    val name: String,
    val description: String?,
    val date: String,
    val menuTypeName: String?,
    val meals: List<MealDto>
)

data class MenuResponseDtoNoDate(
    val menuId: Int,
    val name: String,
    val description: String?,
    val menuTypeName: String?,
    val meals: List<MealDto>
)

data class MenuMealItemDto(
    val mealId: Int
)
data class MenuWriteDto(
    val name: String,
    val description: String?,
    val menuTypeId: Int,
    val meals: List<MenuMealItemDto>
)

data class FavoriteMealDto(
    val mealId: Int,
    val mealTypeId: Int,
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