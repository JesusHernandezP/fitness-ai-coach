package com.fitness.fitnessaicoach.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AIChatMessageRequestDto(
    val message: String,
    val conversationId: String? = null
)
