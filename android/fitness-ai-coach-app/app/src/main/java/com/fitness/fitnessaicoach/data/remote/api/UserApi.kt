package com.fitness.fitnessaicoach.data.remote.api

import com.fitness.fitnessaicoach.data.remote.dto.UserProfileResponseDto
import com.fitness.fitnessaicoach.data.remote.dto.UserResponseDto
import com.fitness.fitnessaicoach.data.remote.dto.UserUpdateRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface UserApi {
    @GET("users/profile")
    suspend fun getCurrentProfile(): UserProfileResponseDto

    @POST("users/profile")
    suspend fun saveUserProfile(@Body request: UserUpdateRequestDto): UserProfileResponseDto

    @PUT("users/profile")
    suspend fun updateUserProfile(@Body request: UserUpdateRequestDto): UserProfileResponseDto
}
