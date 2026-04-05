package com.fitness.fitnessaicoach.data.remote.mapper

import com.fitness.fitnessaicoach.data.remote.dto.AICoachingResponseDto
import com.fitness.fitnessaicoach.domain.model.AICoachAdvice

fun AICoachingResponseDto.toDomain(): AICoachAdvice = AICoachAdvice(
    analysis = analysis,
    advice = advice
)
