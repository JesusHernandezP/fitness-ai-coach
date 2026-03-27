package com.fitness.fitnessaicoach.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.fitness.fitnessaicoach.core.constants.NetworkConstants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DataStoreTokenStorage @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : TokenStorage {

    private val tokenKey = stringPreferencesKey(NetworkConstants.TOKEN_KEY)

    override fun observeToken(): Flow<String?> {
        return dataStore.data.map { preferences -> preferences[tokenKey] }
    }

    override suspend fun getToken(): String? {
        return dataStore.data.first()[tokenKey]
    }

    override suspend fun saveToken(token: String) {
        dataStore.edit { preferences ->
            preferences[tokenKey] = token
        }
    }

    override suspend fun clearToken() {
        dataStore.edit { preferences ->
            preferences.remove(tokenKey)
        }
    }
}
