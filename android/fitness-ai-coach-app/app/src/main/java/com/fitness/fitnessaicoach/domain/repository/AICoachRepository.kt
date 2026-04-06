package com.fitness.fitnessaicoach.domain.repository

import com.fitness.fitnessaicoach.core.result.AppResult
import com.fitness.fitnessaicoach.domain.model.AICoachAdvice

interface AICoachRepository {
    suspend fun getCoaching(dailyLogId: String): AppResult<AICoachAdvice>
}
