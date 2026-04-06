package com.fitness.fitnessaicoach.data.remote.mapper

import com.fitness.fitnessaicoach.data.remote.dto.BodyMetricsRequestDto
import com.fitness.fitnessaicoach.data.remote.dto.BodyMetricsResponseDto
import com.fitness.fitnessaicoach.domain.model.BodyMetrics

fun BodyMetricsResponseDto.toDomain(): BodyMetrics = BodyMetrics(
    id = id,
    userId = userId,
    weight = weight,
    date = date
)

fun BodyMetrics.toRequestDto(): BodyMetricsRequestDto = BodyMetricsRequestDto(
    weight = weight,
    date = date
)
