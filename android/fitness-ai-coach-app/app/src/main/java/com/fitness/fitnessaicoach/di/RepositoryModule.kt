package com.fitness.fitnessaicoach.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.fitness.fitnessaicoach.core.constants.NetworkConstants
import com.fitness.fitnessaicoach.data.chat.AIChatRepository
import com.fitness.fitnessaicoach.data.chat.AIChatRepositoryImpl
import com.fitness.fitnessaicoach.data.local.datastore.DataStoreTokenStorage
import com.fitness.fitnessaicoach.data.local.datastore.TokenStorage
import com.fitness.fitnessaicoach.data.remote.api.AIChatApiService
import com.fitness.fitnessaicoach.data.remote.api.AuthApi
import com.fitness.fitnessaicoach.data.remote.api.BodyMetricsApi
import com.fitness.fitnessaicoach.data.remote.api.CoachApi
import com.fitness.fitnessaicoach.data.remote.api.DailyLogApi
import com.fitness.fitnessaicoach.data.remote.api.GoalsApi
import com.fitness.fitnessaicoach.data.remote.api.UserApi
import com.fitness.fitnessaicoach.data.repository.AICoachRepositoryImpl
import com.fitness.fitnessaicoach.data.repository.AuthRepositoryImpl
import com.fitness.fitnessaicoach.data.repository.BodyMetricsRepositoryImpl
import com.fitness.fitnessaicoach.data.repository.DailyLogRepositoryImpl
import com.fitness.fitnessaicoach.data.repository.GoalsRepositoryImpl
import com.fitness.fitnessaicoach.data.repository.UserRepositoryImpl
import com.fitness.fitnessaicoach.domain.repository.AICoachRepository
import com.fitness.fitnessaicoach.domain.repository.AuthRepository
import com.fitness.fitnessaicoach.domain.repository.BodyMetricsRepository
import com.fitness.fitnessaicoach.domain.repository.DailyLogRepository
import com.fitness.fitnessaicoach.domain.repository.GoalsRepository
import com.fitness.fitnessaicoach.domain.repository.UserRepository
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

    @Provides
    @Singleton
    fun provideBodyMetricsRepository(
        bodyMetricsApi: BodyMetricsApi
    ): BodyMetricsRepository {
        return BodyMetricsRepositoryImpl(
            bodyMetricsApi = bodyMetricsApi
        )
    }

    @Provides
    @Singleton
    fun provideDailyLogRepository(
        dailyLogApi: DailyLogApi,
        userApi: UserApi,
        tokenStorage: TokenStorage
    ): DailyLogRepository {
        return DailyLogRepositoryImpl(
            dailyLogApi = dailyLogApi,
            userApi = userApi,
            tokenStorage = tokenStorage
        )
    }

    @Provides
    @Singleton
    fun provideGoalsRepository(
        goalsApi: GoalsApi
    ): GoalsRepository {
        return GoalsRepositoryImpl(
            goalsApi = goalsApi
        )
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        userApi: UserApi,
        tokenStorage: TokenStorage
    ): UserRepository {
        return UserRepositoryImpl(
            userApi = userApi,
            tokenStorage = tokenStorage
        )
    }

    @Provides
    @Singleton
    fun provideAICoachRepository(
        coachApi: CoachApi
    ): AICoachRepository {
        return AICoachRepositoryImpl(
            coachApi = coachApi
        )
    }

    @Provides
    @Singleton
    fun provideAIChatRepository(
        aiChatApiService: AIChatApiService
    ): AIChatRepository {
        return AIChatRepositoryImpl(
            aiChatApiService = aiChatApiService
        )
    }
}
