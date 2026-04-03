package com.fitness.fitnessaicoach.data.local.datastore

import kotlinx.coroutines.flow.Flow

interface TokenStorage {
    fun observeToken(): Flow<String?>
    suspend fun getToken(): String?
    suspend fun saveToken(token: String)
    suspend fun clearToken()
}
