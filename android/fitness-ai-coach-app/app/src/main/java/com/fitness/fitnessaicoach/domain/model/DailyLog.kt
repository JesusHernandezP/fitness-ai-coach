package com.fitness.fitnessaicoach.domain.model

data class DailyLog(
    val date: String,
    val totalCalories: Double,
    val calorieGoal: Double,
    val protein: Double,
    val meals: Int,
    val workouts: Int
)
