package com.fitness.fitnessaicoach.data.remote.mapper

import com.fitness.fitnessaicoach.data.remote.dto.UserProfileResponseDto
import com.fitness.fitnessaicoach.data.remote.dto.UserResponseDto
import com.fitness.fitnessaicoach.domain.model.GoalType
import com.fitness.fitnessaicoach.domain.model.User

fun UserResponseDto.toDomain(): User = User(
    id = id,
    name = name,
    email = email,
    age = age,
    heightCm = heightCm,
    weightKg = weightKg,
    sex = sex,
    activityLevel = activityLevel,
    dietType = null,
    goalType = null,
    targetCalories = null,
    targetProtein = null,
    targetCarbs = null,
    targetFat = null,
    createdAt = createdAt
)

fun UserProfileResponseDto.toDomain(): User = User(
    id = userId,
    name = "",
    email = "",
    age = age,
    heightCm = heightCm,
    weightKg = weightKg,
    sex = sex,
    activityLevel = activityLevel,
    dietType = dietType,
    goalType = goalType?.let(GoalType::valueOf),
    targetCalories = targetCalories,
    targetProtein = targetProtein,
    targetCarbs = targetCarbs,
    targetFat = targetFat,
    createdAt = ""
)
