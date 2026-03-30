package com.fitness.fitnessaicoach.data.repository

import com.fitness.fitnessaicoach.core.extensions.toErrorMessage
import com.fitness.fitnessaicoach.core.result.AppResult
import com.fitness.fitnessaicoach.data.local.datastore.TokenStorage
import com.fitness.fitnessaicoach.data.remote.api.DailyLogApi
import com.fitness.fitnessaicoach.data.remote.api.UserApi
import com.fitness.fitnessaicoach.data.remote.mapper.toDomain
import com.fitness.fitnessaicoach.data.remote.mapper.toRequestDto
import com.fitness.fitnessaicoach.domain.model.DailyLog
import com.fitness.fitnessaicoach.domain.repository.DailyLogRepository
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.Base64
import javax.inject.Inject

class DailyLogRepositoryImpl @Inject constructor(
    private val dailyLogApi: DailyLogApi,
    private val userApi: UserApi,
    private val tokenStorage: TokenStorage
) : DailyLogRepository {

    override suspend fun getTodayDailyLog(): AppResult<DailyLog> {
        return try {
            AppResult.Success(dailyLogApi.getTodayDailyLog().toDomain())
        } catch (throwable: Throwable) {
            AppResult.Error(message = throwable.toErrorMessage(), throwable = throwable)
        }
    }

    override suspend fun saveDailyLog(dailyLog: DailyLog): DailyLog {
        val resolvedUserId = resolveUserId(dailyLog.userId)
        return dailyLogApi.saveDailyLog(dailyLog.toRequestDto(resolvedUserId)).toDomain()
    }

    private suspend fun resolveUserId(explicitUserId: String?): String {
        if (!explicitUserId.isNullOrBlank()) {
            return explicitUserId
        }

        val token = tokenStorage.getToken()
            ?: throw IllegalStateException("Authentication token not found.")
        val userEmail = extractEmailFromJwt(token)
            ?: throw IllegalStateException("Authenticated user email could not be resolved.")

        val currentUser = userApi.getUsers().firstOrNull { it.email == userEmail }
            ?: throw IllegalStateException("Authenticated user not found.")

        return currentUser.id
    }

    private fun extractEmailFromJwt(token: String): String? {
        val tokenParts = token.split(".")
        if (tokenParts.size < 2) {
            return null
        }

        val payloadBytes = Base64.getUrlDecoder().decode(tokenParts[1])
        val payload = Json.parseToJsonElement(payloadBytes.decodeToString()).jsonObject
        return payload["sub"]?.jsonPrimitive?.content
    }
}
