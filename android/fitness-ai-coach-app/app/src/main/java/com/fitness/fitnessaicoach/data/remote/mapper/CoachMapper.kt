package com.fitness.fitnessaicoach.data.remote.mapper

import com.fitness.fitnessaicoach.data.remote.dto.AICoachingResponseDto
import com.fitness.fitnessaicoach.domain.model.CoachAdvice

fun AICoachingResponseDto.toDomain(): CoachAdvice = CoachAdvice(message = message)
