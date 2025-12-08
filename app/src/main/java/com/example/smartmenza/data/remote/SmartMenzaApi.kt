package com.example.smartmenza.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Query

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
    val uloga: String?
)

data class GoalDto(
    val id: Int,
    val name: String,
    val description: String,
    val progress: Float // 0.0 to 1.0
)

interface SmartMenzaApi {

    @POST("api/Auth/registration")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/Auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("api/menu")
    suspend fun getMenuByDate(
        @Query("date") date: String
    ): Response<MenuResponseDto>

    @GET("api/menu/all")
    suspend fun getMenusByDate(
        @Query("date") date: String
    ): Response<List<MenuResponseDto>>

    @GET("api/User/favorites")
    suspend fun getMyFavorites(): Response<List<MenuResponseDto>>

    @GET("api/User/goals")
    suspend fun getMyGoals(): Response<List<GoalDto>>

}