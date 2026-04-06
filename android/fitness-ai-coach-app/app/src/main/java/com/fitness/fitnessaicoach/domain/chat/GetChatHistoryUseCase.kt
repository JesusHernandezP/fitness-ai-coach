package com.fitness.fitnessaicoach.domain.chat

import com.fitness.fitnessaicoach.core.result.AppResult
import com.fitness.fitnessaicoach.data.chat.AIChatRepository
import javax.inject.Inject

class GetChatHistoryUseCase @Inject constructor(
    private val aiChatRepository: AIChatRepository
) {
    suspend operator fun invoke(): AppResult<List<AIChatHistoryMessage>> {
        return aiChatRepository.getHistory()
    }
}
