package com.example.smartmenza.data.remote

data class MealRatingStatsDto(
    val mealId: Int,
    val numberOfReviews: Int,
    val averageRating: Double
)