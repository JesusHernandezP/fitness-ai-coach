package com.fitness.fitnessaicoach.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AICoachingResponseDto(
    val analysis: String,
    val advice: String
)
