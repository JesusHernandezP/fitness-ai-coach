package com.fitness.fitnessaicoach.domain.model

data class DailyLog(
    val id: String? = null,
    val date: String,
    val totalCalories: Double,
    val calorieGoal: Double,
    val protein: Double,
    val meals: Int,
    val workouts: Int,
    val steps: Int? = null,
    val caloriesConsumed: Double? = null,
    val caloriesBurned: Double? = null,
    val userId: String? = null
)
