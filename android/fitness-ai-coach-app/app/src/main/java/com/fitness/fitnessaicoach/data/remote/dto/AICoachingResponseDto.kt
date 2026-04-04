package com.fitness.fitnessaicoach.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AICoachingResponseDto(
    val analysis: AIAnalysisDto,
    val advice: String
)

@Serializable
data class AIAnalysisDto(
    val dailyLogId: String,
    val userId: String? = null,
    val date: String? = null,
    val totalSteps: Int? = null,
    val totalMeals: Int? = null,
    val totalWorkoutSessions: Int? = null,
    val totalCaloriesConsumed: Double? = null,
    val totalCaloriesBurned: Double? = null,
    val calorieBalance: Double? = null,
    val goalType: String? = null,
    val targetWeight: Double? = null,
    val targetCalories: Double? = null,
    val latestWeight: Double? = null,
    val latestBodyFat: Double? = null,
    val latestMuscleMass: Double? = null
)
