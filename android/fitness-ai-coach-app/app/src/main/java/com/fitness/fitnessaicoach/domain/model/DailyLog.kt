package com.fitness.fitnessaicoach.domain.model

data class DailyLog(
    val id: String,
    val date: String,
    val steps: Int?,
    val caloriesConsumed: Double?,
    val caloriesBurned: Double?,
    val userId: String
)
