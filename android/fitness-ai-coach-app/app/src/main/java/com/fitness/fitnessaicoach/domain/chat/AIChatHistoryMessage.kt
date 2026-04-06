package com.fitness.fitnessaicoach.domain.chat

data class AIChatHistoryMessage(
    val role: String,
    val message: String,
    val createdAt: String? = null
)
