package com.fitness.fitnessaicoach.data.remote.api

import com.fitness.fitnessaicoach.data.remote.dto.DailyLogDto
import retrofit2.http.GET

interface DailyLogApi {
    @GET("daily-log/today")
    suspend fun getTodayDailyLog(): DailyLogDto
}
