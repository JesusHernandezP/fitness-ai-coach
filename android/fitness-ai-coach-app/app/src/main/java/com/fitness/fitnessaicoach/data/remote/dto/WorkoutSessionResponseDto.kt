package com.fitness.fitnessaicoach.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class WorkoutSessionResponseDto(
    val id: String,
    val dailyLogId: String,
    val exerciseId: String,
    val sets: Int? = null,
    val reps: Int? = null,
    val duration: Int? = null,
    val caloriesBurned: Double? = null
)
