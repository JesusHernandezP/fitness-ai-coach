package com.fitness.fitnessaicoach.data.remote.api

import com.fitness.fitnessaicoach.data.remote.dto.LoginRequestDto
import com.fitness.fitnessaicoach.data.remote.dto.LoginResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): LoginResponseDto
}
