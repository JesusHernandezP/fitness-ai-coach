package com.fitness.fitnessaicoach.data.repository

import com.fitness.fitnessaicoach.core.extensions.toErrorMessage
import com.fitness.fitnessaicoach.core.result.AppResult
import com.fitness.fitnessaicoach.data.local.datastore.TokenStorage
import com.fitness.fitnessaicoach.data.remote.api.AuthApi
import com.fitness.fitnessaicoach.data.remote.dto.LoginRequestDto
import com.fitness.fitnessaicoach.data.remote.dto.RegisterRequestDto
import com.fitness.fitnessaicoach.data.remote.mapper.toDomain
import com.fitness.fitnessaicoach.domain.model.AuthToken
import com.fitness.fitnessaicoach.domain.model.UserCredentials
import com.fitness.fitnessaicoach.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import retrofit2.HttpException

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
            val message = if (throwable is HttpException && throwable.code() == 401) {
                "Invalid email or password."
            } else {
                throwable.toErrorMessage()
            }
            AppResult.Error(message = message, throwable = throwable)
        }
    }

    override suspend fun register(credentials: UserCredentials): AppResult<AuthToken> {
        return try {
            authApi.register(
                RegisterRequestDto(
                    name = credentials.email.substringBefore("@").ifBlank { "Fitness User" },
                    email = credentials.email,
                    password = credentials.password
                )
            )

            login(credentials)
        } catch (throwable: Throwable) {
            val message = if (throwable is HttpException && throwable.code() == 400) {
                "Unable to create the account. Check the email and password."
            } else {
                throwable.toErrorMessage()
            }
            AppResult.Error(message = message, throwable = throwable)
        }
    }

    override fun observeToken(): Flow<String?> = tokenStorage.observeToken()
}
