package com.fitness.fitnessaicoach.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AIChatMessageResponseDto(
    val reply: String
)
