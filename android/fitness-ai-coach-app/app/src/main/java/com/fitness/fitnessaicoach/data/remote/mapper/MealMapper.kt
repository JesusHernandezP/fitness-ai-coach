package com.fitness.fitnessaicoach.data.remote.mapper

import com.fitness.fitnessaicoach.data.remote.dto.MealResponseDto
import com.fitness.fitnessaicoach.domain.model.Meal

fun MealResponseDto.toDomain(): Meal = Meal(
    id = id,
    mealType = mealType,
    dailyLogId = dailyLogId
)
