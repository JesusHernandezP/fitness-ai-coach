package com.fitness.fitnessaicoach.data.remote.api

import com.fitness.fitnessaicoach.data.remote.dto.LoginRequestDto
import com.fitness.fitnessaicoach.data.remote.dto.LoginResponseDto
import com.fitness.fitnessaicoach.data.remote.dto.RegisterRequestDto
import com.fitness.fitnessaicoach.data.remote.dto.UserResponseDto
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface AuthApi {
    @Headers(
        "Accept: application/json",
        "Content-Type: application/json"
    )
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): LoginResponseDto

    @Headers(
        "Accept: application/json",
        "Content-Type: application/json"
    )
    @POST("users")
    suspend fun register(@Body request: RegisterRequestDto): UserResponseDto
}
