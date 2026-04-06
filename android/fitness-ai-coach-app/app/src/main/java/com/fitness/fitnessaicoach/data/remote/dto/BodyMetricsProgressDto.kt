package com.fitness.fitnessaicoach.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class BodyMetricsProgressDto(
    val date: String,
    val weight: Double
)
