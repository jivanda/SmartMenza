package com.example.smartmenza.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class RegisterRequest(
    val ime: String,
    val email: String,
    val lozinka: String,
    val uloga: String
)

data class LoginRequest(
    val email: String,
    val lozinka: String
)

data class AuthResponse(
    val poruka: String,
    val ime: String?,
    val email: String?,
    val uloga: String?
)

interface SmartMenzaApi {

    @POST("api/Auth/registracija")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/Auth/prijava")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
}