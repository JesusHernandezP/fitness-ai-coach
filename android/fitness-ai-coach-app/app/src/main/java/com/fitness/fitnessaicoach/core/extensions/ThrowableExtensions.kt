package com.fitness.fitnessaicoach.core.extensions

import java.io.IOException
import retrofit2.HttpException

fun Throwable.toErrorMessage(defaultMessage: String = "Unexpected error"): String {
    return when (this) {
        is HttpException -> {
            when (code()) {
                401 -> "Invalid email or password."
                else -> message()
            }.takeIf { it.isNotBlank() } ?: defaultMessage
        }

        is IOException -> "Unable to reach the server."
        else -> message
    }?.takeIf { it.isNotBlank() } ?: defaultMessage
}
