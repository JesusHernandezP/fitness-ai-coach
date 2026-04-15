package com.fitness.fitnessaicoach.data.remote.api

import com.fitness.fitnessaicoach.data.remote.dto.ApiResponseDto
import com.fitness.fitnessaicoach.data.remote.dto.DailyLogDto
import com.fitness.fitnessaicoach.data.remote.dto.DailyLogRequestDto
import com.fitness.fitnessaicoach.data.remote.dto.WeightUpdateRequestDto
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Body

interface DailyLogApi {
    @GET("daily-logs/today")
    suspend fun getTodayDailyLog(): ApiResponseDto<DailyLogDto>

    @POST("daily-logs")
    suspend fun saveDailyLog(@Body dailyLogRequestDto: DailyLogRequestDto): ApiResponseDto<DailyLogDto>

    @PUT("daily-logs/today/weight")
    suspend fun updateTodayWeight(@Body request: WeightUpdateRequestDto): ApiResponseDto<DailyLogDto>
}
