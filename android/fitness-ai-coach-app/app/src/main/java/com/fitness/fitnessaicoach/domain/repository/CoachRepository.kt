package com.fitness.fitnessaicoach.domain.repository

import com.fitness.fitnessaicoach.domain.model.CoachAdvice

interface CoachRepository {
    suspend fun getCoachingAdvice(): CoachAdvice
}
