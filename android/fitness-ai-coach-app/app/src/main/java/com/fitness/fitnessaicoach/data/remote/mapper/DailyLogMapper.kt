package com.fitness.fitnessaicoach.data.remote.mapper

import com.fitness.fitnessaicoach.data.remote.dto.DailyLogDto
import com.fitness.fitnessaicoach.data.remote.dto.DailyLogRequestDto
import com.fitness.fitnessaicoach.domain.model.DailyLog

fun DailyLogDto.toDomain(): DailyLog = DailyLog(
    id = id,
    date = date,
    totalCalories = caloriesConsumed ?: 0.0,
    calorieGoal = 0.0,
    protein = 0.0,
    meals = 0,
    workouts = 0,
    steps = steps,
    caloriesConsumed = caloriesConsumed,
    caloriesBurned = caloriesBurned,
    userId = userId
)

fun DailyLog.toRequestDto(resolvedUserId: String): DailyLogRequestDto {

    require(!date.isNullOrBlank()) {
        "DailyLog date cannot be null"
    }

    return DailyLogRequestDto(
        logDate = date,
        steps = steps,
        caloriesConsumed = caloriesConsumed ?: 0.0,
        caloriesBurned = caloriesBurned ?: 0.0,
        userId = userId ?: resolvedUserId
    )
}
