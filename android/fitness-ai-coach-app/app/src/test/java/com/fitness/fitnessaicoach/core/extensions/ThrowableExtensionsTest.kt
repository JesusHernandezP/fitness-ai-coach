package com.fitness.fitnessaicoach.core.extensions

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class ThrowableExtensionsTest {

    @Test
    fun `returns backend message for 409 http errors`() {
        val errorBody = """{"message":"You already recorded your weight today"}"""
            .toResponseBody("application/json".toMediaType())
        val response = Response.error<Any>(409, errorBody)

        val message = HttpException(response).toErrorMessage()

        assertEquals("You already recorded your weight today", message)
    }

    @Test
    fun `returns generic unauthorized message for non auth 401 errors`() {
        val response = Response.error<Any>(
            401,
            """{"message":"Token expired"}""".toResponseBody("application/json".toMediaType())
        )

        val message = HttpException(response).toErrorMessage()

        assertEquals("Token expired", message)
    }
}
