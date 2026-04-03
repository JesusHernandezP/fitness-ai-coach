package com.fitness.fitnessaicoach.domain.usecase

import com.fitness.fitnessaicoach.core.result.AppResult
import com.fitness.fitnessaicoach.domain.model.DailyLog
import com.fitness.fitnessaicoach.domain.repository.DailyLogRepository
import javax.inject.Inject

class GetTodayDailyLogUseCase @Inject constructor(
    private val dailyLogRepository: DailyLogRepository
) {
    suspend operator fun invoke(): AppResult<DailyLog> {
        return dailyLogRepository.getTodayDailyLog()
    }
}
