package com.fitness.fitnessaicoach.domain.repository

import com.fitness.fitnessaicoach.domain.model.WorkoutSession

interface WorkoutRepository {
    suspend fun getWorkouts(): List<WorkoutSession>
}
