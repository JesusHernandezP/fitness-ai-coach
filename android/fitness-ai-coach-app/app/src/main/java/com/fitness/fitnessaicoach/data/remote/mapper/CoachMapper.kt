package com.fitness.fitnessaicoach.data.remote.mapper

import com.fitness.fitnessaicoach.data.remote.dto.AICoachingResponseDto
import com.fitness.fitnessaicoach.domain.model.AICoachAdvice

fun AICoachingResponseDto.toDomain(): AICoachAdvice = AICoachAdvice(
    analysis = buildString {
        analysis.date?.let { appendLine("Date: $it") }
        analysis.totalSteps?.let { appendLine("Steps: $it") }
        analysis.totalMeals?.let { appendLine("Meals: $it") }
        analysis.totalWorkoutSessions?.let { appendLine("Workouts: $it") }
        analysis.totalCaloriesConsumed?.let { appendLine("Calories consumed: $it") }
        analysis.totalCaloriesBurned?.let { appendLine("Calories burned: $it") }
        analysis.calorieBalance?.let { appendLine("Calorie balance: $it") }
        analysis.goalType?.let { appendLine("Goal: ${it.replace('_', ' ')}") }
        analysis.targetCalories?.let { appendLine("Target calories: $it") }
        analysis.targetWeight?.let { appendLine("Target weight: $it") }
        analysis.latestWeight?.let { appendLine("Latest weight: $it") }
        analysis.latestBodyFat?.let { appendLine("Latest body fat: $it") }
        analysis.latestMuscleMass?.let { appendLine("Latest muscle mass: $it") }
    }.trim(),
    advice = advice
)
