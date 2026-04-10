package com.fitness.fitnessaicoach.domain.repository

import com.fitness.fitnessaicoach.core.result.AppResult
import com.fitness.fitnessaicoach.domain.model.AuthToken
import com.fitness.fitnessaicoach.domain.model.LoginCredentials
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(credentials: LoginCredentials): AppResult<AuthToken>
    fun observeToken(): Flow<String?>
}
