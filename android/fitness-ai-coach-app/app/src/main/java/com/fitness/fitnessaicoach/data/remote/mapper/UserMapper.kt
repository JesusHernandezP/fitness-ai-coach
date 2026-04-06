package com.fitness.fitnessaicoach.data.remote.mapper

import com.fitness.fitnessaicoach.data.remote.dto.UserResponseDto
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
    createdAt = createdAt
)
