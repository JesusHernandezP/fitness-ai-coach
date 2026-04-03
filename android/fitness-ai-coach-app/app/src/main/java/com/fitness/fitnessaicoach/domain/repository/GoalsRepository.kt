package com.fitness.fitnessaicoach.domain.repository

import com.fitness.fitnessaicoach.core.result.AppResult
import com.fitness.fitnessaicoach.domain.model.Goal

interface GoalsRepository {
    suspend fun createGoal(goal: Goal): AppResult<Goal>
    suspend fun getGoals(): AppResult<List<Goal>>
}
