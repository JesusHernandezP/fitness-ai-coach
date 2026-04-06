package com.fitness.fitnessaicoach.data.chat

import com.fitness.fitnessaicoach.core.result.AppResult
import com.fitness.fitnessaicoach.domain.chat.AIChatHistoryMessage

interface AIChatRepository {
    suspend fun getHistory(): AppResult<List<AIChatHistoryMessage>>
    suspend fun sendMessage(message: String): AppResult<String>
}
