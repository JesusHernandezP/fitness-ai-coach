package com.fitness.fitnessaicoach.domain.repository

import com.fitness.fitnessaicoach.core.result.AppResult
import com.fitness.fitnessaicoach.domain.model.BodyMetrics
import com.fitness.fitnessaicoach.domain.model.WeightProgressPoint

interface BodyMetricsRepository {
    suspend fun createBodyMetrics(bodyMetrics: BodyMetrics): AppResult<BodyMetrics>
    suspend fun getBodyMetrics(): AppResult<List<BodyMetrics>>
    suspend fun getWeightProgress(): AppResult<List<WeightProgressPoint>>
}
