package com.fitness.fitnessaicoach.data.remote.api

import com.fitness.fitnessaicoach.data.remote.dto.ApiResponseDto
import com.fitness.fitnessaicoach.data.remote.dto.BodyMetricsProgressDto
import com.fitness.fitnessaicoach.data.remote.dto.BodyMetricsRequestDto
import com.fitness.fitnessaicoach.data.remote.dto.BodyMetricsResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface BodyMetricsApi {
    @POST("body-metrics")
    suspend fun createBodyMetrics(@Body request: BodyMetricsRequestDto): ApiResponseDto<BodyMetricsResponseDto>

    @GET("body-metrics")
    suspend fun getBodyMetrics(): ApiResponseDto<List<BodyMetricsResponseDto>>

    @GET("body-metrics/progress")
    suspend fun getWeightProgress(): ApiResponseDto<List<BodyMetricsProgressDto>>
}
