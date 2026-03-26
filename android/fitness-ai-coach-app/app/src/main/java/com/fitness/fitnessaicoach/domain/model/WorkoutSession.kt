package com.fitness.fitnessaicoach.domain.model

data class WorkoutSession(
    val id: String,
    val dailyLogId: String,
    val exerciseId: String,
    val sets: Int?,
    val reps: Int?,
    val duration: Int?,
    val caloriesBurned: Double?
)
