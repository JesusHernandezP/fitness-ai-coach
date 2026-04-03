package com.fitness.fitnessaicoach.data.remote.api

import com.fitness.fitnessaicoach.data.local.datastore.TokenStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthTokenInterceptorTest {

    @Test
    fun `does not attach token to login request`() {
        val interceptor = AuthTokenInterceptor(FakeTokenStorage("stale-token"))
        val chain = RecordingChain(requestFor("http://10.0.2.2:8080/api/auth/login"))

        interceptor.intercept(chain)

        assertNull(chain.proceededRequest.header("Authorization"))
    }

    @Test
    fun `attaches token to authenticated request`() {
        val interceptor = AuthTokenInterceptor(FakeTokenStorage("valid-token"))
        val chain = RecordingChain(requestFor("http://10.0.2.2:8080/api/daily-logs"))

        interceptor.intercept(chain)

        assertEquals("Bearer valid-token", chain.proceededRequest.header("Authorization"))
    }

    private fun requestFor(url: String): Request {
        return Request.Builder()
            .url(url)
            .build()
    }

    private class FakeTokenStorage(
        private val token: String?
    ) : TokenStorage {
        override fun observeToken(): Flow<String?> = flowOf(token)

        override suspend fun getToken(): String? = token

        override suspend fun saveToken(token: String) = Unit

        override suspend fun clearToken() = Unit
    }

    private class RecordingChain(
        private val initialRequest: Request
    ) : Interceptor.Chain {
        lateinit var proceededRequest: Request

        override fun request(): Request = initialRequest

        override fun proceed(request: Request): Response {
            proceededRequest = request
            return Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .build()
        }

        override fun connection() = null

        override fun call() = throw UnsupportedOperationException()

        override fun connectTimeoutMillis(): Int = 0

        override fun withConnectTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this

        override fun readTimeoutMillis(): Int = 0

        override fun withReadTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this

        override fun writeTimeoutMillis(): Int = 0

        override fun withWriteTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this
    }
}
