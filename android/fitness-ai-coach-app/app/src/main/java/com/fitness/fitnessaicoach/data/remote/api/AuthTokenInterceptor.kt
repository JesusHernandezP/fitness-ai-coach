package com.fitness.fitnessaicoach.data.remote.api

import com.fitness.fitnessaicoach.data.local.datastore.TokenStorage
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthTokenInterceptor @Inject constructor(
    private val tokenStorage: TokenStorage
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { tokenStorage.getToken() }
        val request = chain.request().newBuilder().apply {
            token?.takeIf { it.isNotBlank() }?.let { header("Authorization", "Bearer $it") }
        }.build()

        return chain.proceed(request)
    }
}
