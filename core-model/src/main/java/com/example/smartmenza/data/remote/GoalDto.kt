package com.example.smartmenza.data.remote

import com.google.gson.annotations.SerializedName

// --- Goals ---
data class GoalDto(
    val goalId: Int,
    val calories: Int,
    val protein: Double,
    val carbohydrates: Double,
    val fat: Double,
    val dateSet: String
)

data class GoalCreateDto(
    @SerializedName("calories")
    val calories: Int,
    @SerializedName("targetProteins")
    val targetProteins: Double,
    @SerializedName("targetCarbs")
    val targetCarbs: Double,
    @SerializedName("targetFats")
    val targetFats: Double
)

data class CreateGoalResponse(
    val message: String,
    val goal: GoalResult
)

data class GoalResult(
    val goalId: Int,
    val calories: Int,
    val protein: Double,
    val carbohydrates: Double,
    val fat: Double,
    val dateSet: String,
    val userId: Int
)
