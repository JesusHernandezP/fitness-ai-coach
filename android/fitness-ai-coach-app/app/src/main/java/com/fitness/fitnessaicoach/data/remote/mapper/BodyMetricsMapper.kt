package com.fitness.fitnessaicoach.data.remote.mapper

import com.fitness.fitnessaicoach.data.remote.dto.BodyMetricsRequestDto
import com.fitness.fitnessaicoach.data.remote.dto.BodyMetricsResponseDto
import com.fitness.fitnessaicoach.domain.model.BodyMetrics

fun BodyMetricsResponseDto.toDomain(): BodyMetrics = BodyMetrics(
    id = id,
    userId = userId,
    weight = weight,
    bodyFat = bodyFat ?: 0.0,
    muscleMass = muscleMass ?: 0.0,
    date = date
)

fun BodyMetrics.toRequestDto(): BodyMetricsRequestDto = BodyMetricsRequestDto(
    weight = weight,
    bodyFat = bodyFat,
    muscleMass = muscleMass,
    date = date
)
