package com.fitness.fitnessaicoach.domain.usecase

import com.fitness.fitnessaicoach.core.result.AppResult
import com.fitness.fitnessaicoach.domain.model.AICoachAdvice
import com.fitness.fitnessaicoach.domain.repository.AICoachRepository
import javax.inject.Inject

class GetDailyCoachingUseCase @Inject constructor(
    private val aiCoachRepository: AICoachRepository
) {
    suspend operator fun invoke(dailyLogId: String): AppResult<AICoachAdvice> {
        return aiCoachRepository.getCoaching(dailyLogId)
    }
}
