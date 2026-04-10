package com.fitness.fitnessaicoach.data.remote.mapper

import com.fitness.fitnessaicoach.data.remote.dto.DailyLogResponseDto
import com.fitness.fitnessaicoach.domain.model.DailyLog

fun DailyLogResponseDto.toDomain(): DailyLog = DailyLog(
    id = id,
    date = date,
    steps = steps,
    caloriesConsumed = caloriesConsumed,
    caloriesBurned = caloriesBurned,
    userId = userId
)
