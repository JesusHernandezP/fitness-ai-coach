package com.fitness.fitnessaicoach.data.repository

import com.fitness.fitnessaicoach.core.extensions.toErrorMessage
import com.fitness.fitnessaicoach.core.result.AppResult
import com.fitness.fitnessaicoach.data.local.datastore.TokenStorage
import com.fitness.fitnessaicoach.data.remote.api.UserApi
import com.fitness.fitnessaicoach.data.remote.dto.UserUpdateRequestDto
import com.fitness.fitnessaicoach.data.remote.mapper.toDomain
import com.fitness.fitnessaicoach.domain.model.User
import com.fitness.fitnessaicoach.domain.repository.UserRepository
import java.util.Base64
import javax.inject.Inject
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class UserRepositoryImpl @Inject constructor(
    private val userApi: UserApi,
    private val tokenStorage: TokenStorage
) : UserRepository {

    override suspend fun getCurrentUser(): AppResult<User> {
        return try {
            AppResult.Success(resolveCurrentUser())
        } catch (throwable: Throwable) {
            AppResult.Error(message = throwable.toErrorMessage(), throwable = throwable)
        }
    }

    override suspend fun updateUserProfile(user: User): AppResult<User> {
        return try {
            val currentUser = resolveCurrentUser()
            val updatedUser = userApi.updateUserProfile(
                id = currentUser.id,
                request = UserUpdateRequestDto(
                    age = user.age ?: throw IllegalArgumentException("Age is required."),
                    heightCm = user.heightCm ?: throw IllegalArgumentException("Height is required."),
                    sex = user.sex ?: throw IllegalArgumentException("Sex is required."),
                    activityLevel = user.activityLevel ?: throw IllegalArgumentException("Activity level is required.")
                )
            ).toDomain()

            AppResult.Success(updatedUser)
        } catch (throwable: Throwable) {
            AppResult.Error(message = throwable.toErrorMessage(), throwable = throwable)
        }
    }

    private suspend fun resolveCurrentUser(): User {
        val token = tokenStorage.getToken()
            ?: throw IllegalStateException("Authentication token not found.")
        val userEmail = extractEmailFromJwt(token)
            ?: throw IllegalStateException("Authenticated user email could not be resolved.")

        return userApi.getUsers().firstOrNull { it.email == userEmail }?.toDomain()
            ?: throw IllegalStateException("Authenticated user not found.")
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
