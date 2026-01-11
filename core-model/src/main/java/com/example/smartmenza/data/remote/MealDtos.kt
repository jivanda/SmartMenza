package com.example.smartmenza.data.remote

data class MealDto(
    val mealId: Int,
    val mealTypeId: Int,
    val name: String,
    val description: String?,
    val price: Double,
    val calories: Double?,
    val protein: Double?,
    val carbohydrates: Double?,
    val fat: Double?
)