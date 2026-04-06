package com.fitness.fitnessaicoach.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AIChatHistoryMessageDto(
    val role: String,
    val message: String,
    val createdAt: String? = null
)
