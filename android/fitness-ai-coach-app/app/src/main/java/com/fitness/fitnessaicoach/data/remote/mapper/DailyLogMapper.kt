package com.fitness.fitnessaicoach.data.remote.mapper

import com.fitness.fitnessaicoach.data.remote.dto.DailyLogDto
import com.fitness.fitnessaicoach.domain.model.DailyLog

fun DailyLogDto.toDomain(): DailyLog = DailyLog(
    date = date,
    totalCalories = caloriesConsumed ?: 0.0,
    calorieGoal = 0.0,
    protein = 0.0,
    meals = 0,
    workouts = 0
)
