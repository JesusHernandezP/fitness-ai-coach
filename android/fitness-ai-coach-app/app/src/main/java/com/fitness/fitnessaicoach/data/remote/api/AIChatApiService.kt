package com.fitness.fitnessaicoach.data.remote.api

import com.fitness.fitnessaicoach.data.remote.dto.AIChatHistoryMessageDto
import com.fitness.fitnessaicoach.data.remote.dto.AIChatMessageRequestDto
import com.fitness.fitnessaicoach.data.remote.dto.AIChatMessageResponseDto
import retrofit2.http.GET
import retrofit2.http.Body
import retrofit2.http.POST

interface AIChatApiService {
    @GET("ai-chat/history")
    suspend fun getHistory(): List<AIChatHistoryMessageDto>

    @POST("ai-chat/message")
    suspend fun sendMessage(@Body request: AIChatMessageRequestDto): AIChatMessageResponseDto
}
