package com.fitness.fitnessaicoach.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class BodyMetricsRequestDto(
    val weight: Double,
    val date: String
)
