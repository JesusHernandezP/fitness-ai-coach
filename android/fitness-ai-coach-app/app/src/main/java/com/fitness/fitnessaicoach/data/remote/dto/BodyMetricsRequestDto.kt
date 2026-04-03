package com.fitness.fitnessaicoach.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class BodyMetricsRequestDto(
    val weight: Double,
    val bodyFat: Double,
    val muscleMass: Double,
    val date: String
)
