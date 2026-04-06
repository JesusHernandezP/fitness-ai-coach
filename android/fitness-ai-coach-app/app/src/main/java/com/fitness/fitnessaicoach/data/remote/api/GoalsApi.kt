package com.fitness.fitnessaicoach.data.remote.api

import com.fitness.fitnessaicoach.data.remote.dto.GoalsRequestDto
import com.fitness.fitnessaicoach.data.remote.dto.GoalsResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface GoalsApi {

    @POST("goals")
    suspend fun createGoal(@Body request: GoalsRequestDto): GoalsResponseDto

    @GET("goals")
    suspend fun getGoals(): List<GoalsResponseDto>
}
