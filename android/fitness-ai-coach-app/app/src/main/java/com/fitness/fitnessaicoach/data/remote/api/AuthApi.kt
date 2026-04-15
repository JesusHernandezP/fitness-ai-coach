package com.fitness.fitnessaicoach.data.remote.api

import com.fitness.fitnessaicoach.data.remote.dto.ApiResponseDto
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
    suspend fun login(@Body request: LoginRequestDto): ApiResponseDto<LoginResponseDto>

    @Headers(
        "Accept: application/json",
        "Content-Type: application/json"
    )
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequestDto): ApiResponseDto<UserResponseDto>
}
