package com.fitness.fitnessaicoach.domain.usecase

import com.fitness.fitnessaicoach.core.result.AppResult
import com.fitness.fitnessaicoach.domain.model.WeightProgressPoint
import com.fitness.fitnessaicoach.domain.repository.BodyMetricsRepository
import javax.inject.Inject

class GetWeightProgressUseCase @Inject constructor(
    private val bodyMetricsRepository: BodyMetricsRepository
) {
    suspend operator fun invoke(): AppResult<List<WeightProgressPoint>> {
        return bodyMetricsRepository.getWeightProgress()
    }
}
