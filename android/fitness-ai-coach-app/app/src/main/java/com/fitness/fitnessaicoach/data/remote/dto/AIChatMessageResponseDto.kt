package com.fitness.fitnessaicoach.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AIChatMessageResponseDto(
    val response: String
)
