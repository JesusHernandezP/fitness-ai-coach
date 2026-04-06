package com.fitness.fitnessaicoach.data.chat

import com.fitness.fitnessaicoach.core.extensions.toErrorMessage
import com.fitness.fitnessaicoach.core.result.AppResult
import com.fitness.fitnessaicoach.data.remote.api.AIChatApiService
import com.fitness.fitnessaicoach.data.remote.dto.AIChatMessageRequestDto
import com.fitness.fitnessaicoach.domain.chat.AIChatHistoryMessage
import javax.inject.Inject

class AIChatRepositoryImpl @Inject constructor(
    private val aiChatApiService: AIChatApiService
) : AIChatRepository {

    override suspend fun getHistory(): AppResult<List<AIChatHistoryMessage>> {
        return try {
            AppResult.Success(
                aiChatApiService.getHistory().map { dto ->
                    AIChatHistoryMessage(
                        role = dto.role,
                        message = dto.message,
                        createdAt = dto.createdAt
                    )
                }
            )
        } catch (throwable: Throwable) {
            AppResult.Error(
                message = throwable.toErrorMessage("Unable to load chat history."),
                throwable = throwable
            )
        }
    }

    override suspend fun sendMessage(message: String): AppResult<String> {
        return try {
            val response = aiChatApiService.sendMessage(AIChatMessageRequestDto(message = message))
            AppResult.Success(response.reply)
        } catch (throwable: Throwable) {
            AppResult.Error(
                message = throwable.toErrorMessage("Unable to send message."),
                throwable = throwable
            )
        }
    }
}
