package com.fitness.fitnessaicoach.data.remote.api

import com.fitness.fitnessaicoach.data.remote.dto.UserResponseDto
import retrofit2.http.GET

interface UserApi {
    @GET("users")
    suspend fun getUsers(): List<UserResponseDto>
}
