package com.fitness.fitnessaicoach.presentation.chat

import java.util.UUID

data class AIChatUiState(
    val messages: List<AIChatMessageItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class AIChatMessageItem(
    val id: String = UUID.randomUUID().toString(),
    val role: AIChatRole,
    val content: String,
    val timestamp: Long? = System.currentTimeMillis()
)

enum class AIChatRole {
    USER,
    ASSISTANT
}
