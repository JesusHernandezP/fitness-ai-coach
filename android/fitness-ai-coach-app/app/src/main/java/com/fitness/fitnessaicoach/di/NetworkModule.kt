package com.fitness.fitnessaicoach.di

import com.fitness.fitnessaicoach.BuildConfig
import com.fitness.fitnessaicoach.data.remote.api.ApiClient
import com.fitness.fitnessaicoach.data.remote.api.AuthApi
import com.fitness.fitnessaicoach.data.remote.api.AuthTokenInterceptor
import com.fitness.fitnessaicoach.data.remote.api.CoachApi
import com.fitness.fitnessaicoach.data.remote.api.DailyLogApi
import com.fitness.fitnessaicoach.data.remote.api.MealApi
import com.fitness.fitnessaicoach.data.remote.api.WorkoutApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authTokenInterceptor: AuthTokenInterceptor): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(authTokenInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        return ApiClient.createRetrofit(
            baseUrl = BuildConfig.API_BASE_URL,
            okHttpClient = okHttpClient,
            json = json
        )
    }

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideDailyLogApi(retrofit: Retrofit): DailyLogApi = retrofit.create(DailyLogApi::class.java)

    @Provides
    @Singleton
    fun provideMealApi(retrofit: Retrofit): MealApi = retrofit.create(MealApi::class.java)

    @Provides
    @Singleton
    fun provideWorkoutApi(retrofit: Retrofit): WorkoutApi = retrofit.create(WorkoutApi::class.java)

    @Provides
    @Singleton
    fun provideCoachApi(retrofit: Retrofit): CoachApi = retrofit.create(CoachApi::class.java)
}
