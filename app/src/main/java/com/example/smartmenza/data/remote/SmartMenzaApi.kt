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

}