package com.fitness.fitnessaicoach.data.remote.api

import com.fitness.fitnessaicoach.data.remote.dto.DailyLogDto
import com.fitness.fitnessaicoach.data.remote.dto.DailyLogRequestDto
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body

interface DailyLogApi {
    @GET("daily-logs/today")
    suspend fun getTodayDailyLog(): DailyLogDto

    @POST("daily-logs")
    suspend fun saveDailyLog(@Body dailyLogRequestDto: DailyLogRequestDto): DailyLogDto
}
