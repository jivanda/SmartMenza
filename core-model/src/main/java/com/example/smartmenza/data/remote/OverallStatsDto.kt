package com.example.smartmenza.data.remote

data class OverallStatsDto(
    val dateFrom: String?,
    val dateTo: String?,
    val totalMeals: Int,
    val overallAverageRating: Double,
    val maxRating: Double
)