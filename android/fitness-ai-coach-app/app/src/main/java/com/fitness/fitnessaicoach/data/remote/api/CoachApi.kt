package com.fitness.fitnessaicoach.data.remote.api

import com.fitness.fitnessaicoach.data.remote.dto.AICoachingResponseDto
import retrofit2.http.GET
import retrofit2.http.Path

interface CoachApi {
    @GET("ai-coach/daily-log/{dailyLogId}")
    suspend fun getDailyCoaching(@Path("dailyLogId") dailyLogId: String): AICoachingResponseDto
}
