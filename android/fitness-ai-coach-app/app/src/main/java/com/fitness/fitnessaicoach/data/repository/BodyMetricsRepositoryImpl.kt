package com.fitness.fitnessaicoach.data.repository

import com.fitness.fitnessaicoach.core.extensions.toErrorMessage
import com.fitness.fitnessaicoach.core.result.AppResult
import com.fitness.fitnessaicoach.data.remote.api.BodyMetricsApi
import com.fitness.fitnessaicoach.data.remote.mapper.toDomain
import com.fitness.fitnessaicoach.data.remote.mapper.toRequestDto
import com.fitness.fitnessaicoach.domain.model.BodyMetrics
import com.fitness.fitnessaicoach.domain.model.WeightProgressPoint
import com.fitness.fitnessaicoach.domain.repository.BodyMetricsRepository
import javax.inject.Inject

class BodyMetricsRepositoryImpl @Inject constructor(
    private val bodyMetricsApi: BodyMetricsApi
) : BodyMetricsRepository {

    override suspend fun createBodyMetrics(bodyMetrics: BodyMetrics): AppResult<BodyMetrics> {
        return try {
            AppResult.Success(bodyMetricsApi.createBodyMetrics(bodyMetrics.toRequestDto()).data.toDomain())
        } catch (throwable: Throwable) {
            AppResult.Error(message = throwable.toErrorMessage(), throwable = throwable)
        }
    }

    override suspend fun getBodyMetrics(): AppResult<List<BodyMetrics>> {
        return try {
            AppResult.Success(bodyMetricsApi.getBodyMetrics().data.map { it.toDomain() })
        } catch (throwable: Throwable) {
            AppResult.Error(message = throwable.toErrorMessage(), throwable = throwable)
        }
    }

    override suspend fun getWeightProgress(): AppResult<List<WeightProgressPoint>> {
        return try {
            AppResult.Success(bodyMetricsApi.getWeightProgress().data.map { it.toDomain() })
        } catch (throwable: Throwable) {
            AppResult.Error(message = throwable.toErrorMessage(), throwable = throwable)
        }
    }
}
