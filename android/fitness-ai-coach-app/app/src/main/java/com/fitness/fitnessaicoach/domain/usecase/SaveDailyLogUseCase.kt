package com.fitness.fitnessaicoach.domain.usecase

import com.fitness.fitnessaicoach.core.extensions.toErrorMessage
import com.fitness.fitnessaicoach.core.result.AppResult
import com.fitness.fitnessaicoach.domain.model.DailyLog
import com.fitness.fitnessaicoach.domain.repository.DailyLogRepository
import javax.inject.Inject

class SaveDailyLogUseCase @Inject constructor(
    private val dailyLogRepository: DailyLogRepository
) {
    suspend operator fun invoke(dailyLog: DailyLog): AppResult<DailyLog> {
        return try {
            AppResult.Success(dailyLogRepository.saveDailyLog(dailyLog))
        } catch (throwable: Throwable) {
            AppResult.Error(message = throwable.toErrorMessage(), throwable = throwable)
        }
    }
}
