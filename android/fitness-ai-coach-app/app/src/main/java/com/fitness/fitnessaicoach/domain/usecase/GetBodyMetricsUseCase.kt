package com.fitness.fitnessaicoach.domain.usecase

import com.fitness.fitnessaicoach.core.result.AppResult
import com.fitness.fitnessaicoach.domain.model.BodyMetrics
import com.fitness.fitnessaicoach.domain.repository.BodyMetricsRepository
import javax.inject.Inject

class GetBodyMetricsUseCase @Inject constructor(
    private val bodyMetricsRepository: BodyMetricsRepository
) {
    suspend operator fun invoke(): AppResult<List<BodyMetrics>> {
        return bodyMetricsRepository.getBodyMetrics()
    }
}
