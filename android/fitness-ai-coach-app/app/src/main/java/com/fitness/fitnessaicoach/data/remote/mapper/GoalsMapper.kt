package com.fitness.fitnessaicoach.data.remote.mapper

import com.fitness.fitnessaicoach.data.remote.dto.GoalsRequestDto
import com.fitness.fitnessaicoach.data.remote.dto.GoalsResponseDto
import com.fitness.fitnessaicoach.domain.model.Goal
import com.fitness.fitnessaicoach.domain.model.GoalType

fun GoalsResponseDto.toDomain(): Goal = Goal(
    id = id,
    userId = userId,
    goalType = GoalType.valueOf(goalType),
    targetWeight = targetWeight,
    targetCalories = targetCalories
)

fun Goal.toRequestDto(): GoalsRequestDto = GoalsRequestDto(
    goalType = goalType.name,
    targetWeight = targetWeight,
    targetCalories = targetCalories
)
