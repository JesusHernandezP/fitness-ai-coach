package com.fitness.fitnessaicoach.domain.repository

import com.fitness.fitnessaicoach.core.result.AppResult
import com.fitness.fitnessaicoach.domain.model.AuthToken
import com.fitness.fitnessaicoach.domain.model.UserCredentials
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(credentials: UserCredentials): AppResult<AuthToken>
    suspend fun register(name: String, email: String, password: String): AppResult<Unit>
    fun observeToken(): Flow<String?>
}
