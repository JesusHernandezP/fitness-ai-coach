package com.fitness.fitnessaicoach.data.repository

import com.fitness.fitnessaicoach.core.extensions.toErrorMessage
import com.fitness.fitnessaicoach.core.result.AppResult
import com.fitness.fitnessaicoach.data.remote.api.GoalsApi
import com.fitness.fitnessaicoach.data.remote.mapper.toDomain
import com.fitness.fitnessaicoach.data.remote.mapper.toRequestDto
import com.fitness.fitnessaicoach.domain.model.Goal
import com.fitness.fitnessaicoach.domain.repository.GoalsRepository
import javax.inject.Inject

class GoalsRepositoryImpl @Inject constructor(
    private val goalsApi: GoalsApi
) : GoalsRepository {

    override suspend fun createGoal(goal: Goal): AppResult<Goal> {
        return try {
            AppResult.Success(goalsApi.createGoal(goal.toRequestDto()).toDomain())
        } catch (throwable: Throwable) {
            AppResult.Error(message = throwable.toErrorMessage(), throwable = throwable)
        }
    }

    override suspend fun getGoals(): AppResult<List<Goal>> {
        return try {
            AppResult.Success(goalsApi.getGoals().map { it.toDomain() })
        } catch (throwable: Throwable) {
            AppResult.Error(message = throwable.toErrorMessage(), throwable = throwable)
        }
    }
}
