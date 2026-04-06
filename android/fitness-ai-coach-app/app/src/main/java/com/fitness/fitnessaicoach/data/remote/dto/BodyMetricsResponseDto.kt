package com.fitness.fitnessaicoach.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class BodyMetricsResponseDto(
    val id: String,
    val userId: String,
    val weight: Double,
    val date: String
)
