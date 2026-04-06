package com.fitness.fitnessaicoach.data.chat

import com.fitness.fitnessaicoach.core.result.AppResult

interface AIChatRepository {
    suspend fun sendMessage(message: String): AppResult<String>
}
