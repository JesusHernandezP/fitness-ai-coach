package com.fitness.fitnessaicoach.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.fitness.fitnessaicoach.core.constants.NetworkConstants
import com.fitness.fitnessaicoach.data.local.datastore.DataStoreTokenStorage
import com.fitness.fitnessaicoach.data.local.datastore.TokenStorage
import com.fitness.fitnessaicoach.data.remote.api.AuthApi
import com.fitness.fitnessaicoach.data.repository.AuthRepositoryImpl
import com.fitness.fitnessaicoach.domain.repository.AuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.preferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = NetworkConstants.TOKEN_DATASTORE
)

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun providePreferencesDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> = context.preferencesDataStore

    @Provides
    @Singleton
    fun provideTokenStorage(dataStore: DataStore<Preferences>): TokenStorage {
        return DataStoreTokenStorage(dataStore)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        authApi: AuthApi,
        tokenStorage: TokenStorage
    ): AuthRepository {
        return AuthRepositoryImpl(
            authApi = authApi,
            tokenStorage = tokenStorage
        )
    }
}
