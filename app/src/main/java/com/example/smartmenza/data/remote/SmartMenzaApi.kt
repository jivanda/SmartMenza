package com.example.smartmenza.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*

// --- Auth ---
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val roleName: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val poruka: String,
    val username: String?,
    val email: String?,
    val uloga: String?,
    val userId: Int?
)

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

data class GoogleLoginRequest(val idToken: String)

interface SmartMenzaApi {

    @POST("api/Auth/registration")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/Auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    // --- Menus ---
    @GET("api/menu/all")
    suspend fun getMenusByDate(@Query("date") date: String): Response<List<MenuResponseDto>>

    // --- Goals ---
    @GET("api/Goal/myGoal")
    suspend fun getMyGoals(@Header("UserId") userId: Int): Response<List<GoalDto>>

    @POST("api/Goal/create")
    suspend fun createGoal(@Header("UserId") userId: Int, @Body request: GoalCreateDto): Response<CreateGoalResponse>

    @DELETE("api/Goal/{goalId}")
    suspend fun deleteGoal(@Path("goalId") goalId: Int, @Header("UserId") userId: Int): Response<Unit>

    @PUT("api/Goal/{goalId}")
    suspend fun updateGoal(@Path("goalId") goalId: Int, @Header("UserId") userId: Int, @Body request: GoalCreateDto): Response<Unit>

    // --- Favorites ---
    @GET("api/Favorite/my")
    suspend fun getMyFavorites(@Header("UserId") userId: Int): Response<List<FavoriteMealDto>>

    @POST("api/Favorite/add")
    suspend fun addFavorite(@Header("UserId") userId: Int, @Body dto: FavoriteToggleDto): Response<Unit>

    @HTTP(method = "DELETE", path = "api/Favorite/remove", hasBody = true)
    suspend fun removeFavorite(@Header("UserId") userId: Int, @Body dto: FavoriteToggleDto): Response<Unit>

    @GET("api/Favorite/status/{mealId}")
    suspend fun getFavoriteStatus(@Path("mealId") mealId: Int, @Header("UserId") userId: Int): Response<FavoriteStatusDto>

    @POST("api/Auth/google")
    suspend fun googleLogin(@Body request: GoogleLoginRequest): Response<AuthResponse>
}