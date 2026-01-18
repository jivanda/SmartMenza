package com.example.smartmenza.data.remote

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
    val role: String?,
    val userId: Int?
)


data class SimpleResponse(
    val message: String?
)


data class GoogleLoginRequest(val idToken: String)

interface SmartMenzaApi {

    @POST("api/Auth/registration")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/Auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    // --- Menus ---
    @GET("api/Menu")
    suspend fun getMenusByDate(
        @Query("date") date: String
    ): Response<List<MenuResponseDto>>


    @GET("api/menu/{menuId}")
    suspend fun getMenuById(@Path("menuId") menuId: Int): Response<MenuResponseDtoNoDate>

    @GET("api/menu/by-type")
    suspend fun getMenusByType(@Query("menuTypeId") menuTypeId: Int): Response<List<MenuResponseDto>>

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

    @DELETE("api/Menu/admin/{menuId}")
    suspend fun deleteMenu(
        @Path("menuId") menuId: Int,
        @Header("Uloga") role: String
    ): retrofit2.Response<SimpleResponse>

    @GET("api/Meal")
    suspend fun getAllMeals(): Response<List<MealDto>>

    @GET("api/meal/{mealId}")
    suspend fun getMealById(@Path("mealId") menuId: Int): Response<MealDto>

    @GET("api/MealType/name")
    suspend fun getMealTypeName(@Query("mealTypeId") mealTypeId: Int): Response<String>

    @POST("api/Menu/admin/nodate")
    suspend fun createMenu(
        @Header("Uloga") role: String,
        @Body dto: MenuWriteDto
    ): Response<SimpleResponse>

    @PUT("api/Menu/admin/{menuId}")
    suspend fun updateMenu(
        @Path("menuId") menuId: Int,
        @Header("Uloga") role: String,
        @Body dto: MenuWriteDto
    ): Response<SimpleResponse>
}
