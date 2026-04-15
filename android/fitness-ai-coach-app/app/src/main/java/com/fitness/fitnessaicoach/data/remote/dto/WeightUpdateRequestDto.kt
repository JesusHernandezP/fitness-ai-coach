package com.fitness.fitnessaicoach.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class WeightUpdateRequestDto(
    val weightKg: Double
)
