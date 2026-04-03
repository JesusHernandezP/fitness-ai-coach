package com.fitness.fitnessaicoach.domain.usecase

import com.fitness.fitnessaicoach.core.result.AppResult
import com.fitness.fitnessaicoach.domain.model.Goal
import com.fitness.fitnessaicoach.domain.repository.GoalsRepository
import javax.inject.Inject

class GetGoalsUseCase @Inject constructor(
    private val goalsRepository: GoalsRepository
) {
    suspend operator fun invoke(): AppResult<List<Goal>> {
        return goalsRepository.getGoals()
    }
}
