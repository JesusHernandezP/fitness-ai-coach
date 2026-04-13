package com.fitness.fitnessaicoach.data.chat

import com.fitness.fitnessaicoach.core.extensions.toErrorMessage
import com.fitness.fitnessaicoach.core.result.AppResult
import com.fitness.fitnessaicoach.data.remote.api.AIChatApiService
import com.fitness.fitnessaicoach.data.remote.dto.AIChatMessageRequestDto
import javax.inject.Inject

class AIChatRepositoryImpl @Inject constructor(
    private val aiChatApiService: AIChatApiService
) : AIChatRepository {

    override suspend fun sendMessage(message: String): AppResult<String> {
        return try {
            val response = aiChatApiService.sendMessage(AIChatMessageRequestDto(message = message))
            AppResult.Success(response.response)
        } catch (throwable: Throwable) {
            AppResult.Error(
                message = throwable.toErrorMessage("Unable to send message."),
                throwable = throwable
            )
        }
    }
}
