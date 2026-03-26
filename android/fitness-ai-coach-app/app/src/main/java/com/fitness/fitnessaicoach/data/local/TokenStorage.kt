package com.fitness.fitnessaicoach.data.local

import kotlinx.coroutines.flow.Flow

interface TokenStorage {
    fun observeToken(): Flow<String?>
    suspend fun saveToken(token: String)
    suspend fun clearToken()
}
