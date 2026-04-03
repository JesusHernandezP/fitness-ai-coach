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
        val originalRequest = chain.request()
        if (originalRequest.url.encodedPath in UNAUTHENTICATED_PATHS) {
            return chain.proceed(originalRequest)
        }

        val token = runBlocking { tokenStorage.getToken() }
        val request = originalRequest.newBuilder().apply {
            token?.takeIf { it.isNotBlank() }?.let { header("Authorization", "Bearer $it") }
        }.build()

        return chain.proceed(request)
    }

    private companion object {
        private val UNAUTHENTICATED_PATHS = setOf(
            "/api/auth/login",
            "/api/users"
        )
    }
}
