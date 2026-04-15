package com.fitness.fitnessaicoach.core.extensions

import java.io.IOException
import org.json.JSONObject
import retrofit2.HttpException

fun Throwable.toErrorMessage(defaultMessage: String = "Unexpected error"): String {
    return when (this) {
        is HttpException -> {
            val backendMessage = runCatching {
                val rawBody = response()?.errorBody()?.string()?.takeIf { it.isNotBlank() }
                rawBody?.let { body ->
                    val jsonMessage = runCatching { JSONObject(body).optString("message") }.getOrNull()
                    jsonMessage?.takeIf { it.isNotBlank() }
                        ?: Regex(""""message"\s*:\s*"([^"]+)"""")
                            .find(body)
                            ?.groupValues
                            ?.getOrNull(1)
                            ?.takeIf { it.isNotBlank() }
                }
            }.getOrNull()

            when (code()) {
                401 -> backendMessage ?: "Invalid email or password."
                409 -> backendMessage ?: message()
                else -> backendMessage ?: message()
            }.takeIf { it.isNotBlank() } ?: defaultMessage
        }

        is IOException -> "Unable to reach the server."
        else -> message
    }?.takeIf { it.isNotBlank() } ?: defaultMessage
}
