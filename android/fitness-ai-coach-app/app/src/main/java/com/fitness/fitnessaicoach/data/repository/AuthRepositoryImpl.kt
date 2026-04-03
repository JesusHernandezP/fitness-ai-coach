package com.fitness.fitnessaicoach.data.repository

import com.fitness.fitnessaicoach.core.extensions.toErrorMessage
import com.fitness.fitnessaicoach.core.result.AppResult
import com.fitness.fitnessaicoach.data.local.datastore.TokenStorage
import com.fitness.fitnessaicoach.data.remote.api.AuthApi
import com.fitness.fitnessaicoach.data.remote.dto.LoginRequestDto
import com.fitness.fitnessaicoach.data.remote.mapper.toDomain
import com.fitness.fitnessaicoach.domain.model.AuthToken
import com.fitness.fitnessaicoach.domain.model.UserCredentials
import com.fitness.fitnessaicoach.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val tokenStorage: TokenStorage
) : AuthRepository {

    override suspend fun login(credentials: UserCredentials): AppResult<AuthToken> {
        return try {
            val response = authApi.login(
                LoginRequestDto(
                    email = credentials.email,
                    password = credentials.password
                )
            )
            tokenStorage.saveToken(response.token)
            AppResult.Success(response.toDomain())
        } catch (throwable: Throwable) {
            AppResult.Error(message = throwable.toErrorMessage(), throwable = throwable)
        }
    }

    override fun observeToken(): Flow<String?> = tokenStorage.observeToken()
}
