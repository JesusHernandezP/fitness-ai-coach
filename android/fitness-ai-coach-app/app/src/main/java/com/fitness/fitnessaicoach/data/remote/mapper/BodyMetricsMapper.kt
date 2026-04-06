package com.fitness.fitnessaicoach.data.remote.mapper

import com.fitness.fitnessaicoach.data.remote.dto.BodyMetricsProgressDto
import com.fitness.fitnessaicoach.data.remote.dto.BodyMetricsRequestDto
import com.fitness.fitnessaicoach.data.remote.dto.BodyMetricsResponseDto
import com.fitness.fitnessaicoach.domain.model.BodyMetrics
import com.fitness.fitnessaicoach.domain.model.WeightProgressPoint

fun BodyMetricsResponseDto.toDomain(): BodyMetrics = BodyMetrics(
    id = id,
    userId = userId,
    weight = weight,
    date = date
)

fun BodyMetricsProgressDto.toDomain(): WeightProgressPoint = WeightProgressPoint(
    date = date,
    weight = weight
)

fun BodyMetrics.toRequestDto(): BodyMetricsRequestDto = BodyMetricsRequestDto(
    weight = weight,
    date = date
)
