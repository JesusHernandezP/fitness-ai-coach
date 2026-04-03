package com.fitness.fitnessaicoach.domain.usecase

import com.fitness.fitnessaicoach.core.result.AppResult
import com.fitness.fitnessaicoach.domain.model.BodyMetrics
import com.fitness.fitnessaicoach.domain.repository.BodyMetricsRepository
import javax.inject.Inject

class CreateBodyMetricsUseCase @Inject constructor(
    private val bodyMetricsRepository: BodyMetricsRepository
) {
    suspend operator fun invoke(bodyMetrics: BodyMetrics): AppResult<BodyMetrics> {
        return bodyMetricsRepository.createBodyMetrics(bodyMetrics)
    }
}
