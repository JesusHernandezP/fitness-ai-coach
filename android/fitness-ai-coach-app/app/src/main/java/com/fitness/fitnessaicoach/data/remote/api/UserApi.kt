package com.fitness.fitnessaicoach.data.remote.api

import com.fitness.fitnessaicoach.data.remote.dto.ApiResponseDto
import com.fitness.fitnessaicoach.data.remote.dto.UserResponseDto
import com.fitness.fitnessaicoach.data.remote.dto.UserUpdateRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface UserApi {
    @GET("users")
    suspend fun getUsers(): ApiResponseDto<List<UserResponseDto>>

    @PUT("users/{id}")
    suspend fun updateUserProfile(
        @Path("id") id: String,
        @Body request: UserUpdateRequestDto
    ): ApiResponseDto<UserResponseDto>
}
