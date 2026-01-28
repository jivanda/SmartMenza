package com.example.smartmenza.data.remote

data class RatingCommentCreateDto(
    val mealId: Int,
    val rating: Int,
    val comment: String?
)

data class RatingCommentUpdateDto(
    val rating: Int,
    val comment: String?
)

data class RatingCommentDto(
    val mealId: Int,
    val userId: Int,
    val username: String,
    val rating: Int,
    val comment: String?
)

data class RatingSummaryDto(
    val mealId: Int,
    val ratingsCount: Int,
    val averageRating: Double
)
