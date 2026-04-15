package com.fitness.fitnessaicoach.domain.usecase

import com.fitness.fitnessaicoach.core.extensions.toErrorMessage
import com.fitness.fitnessaicoach.core.result.AppResult
import com.fitness.fitnessaicoach.domain.model.DailyLog
import com.fitness.fitnessaicoach.domain.repository.DailyLogRepository
import javax.inject.Inject

class UpdateTodayWeightUseCase @Inject constructor(
    private val dailyLogRepository: DailyLogRepository
) {
    suspend operator fun invoke(weightKg: Double): AppResult<DailyLog> {
        return dailyLogRepository.updateTodayWeight(weightKg)
    }
}
