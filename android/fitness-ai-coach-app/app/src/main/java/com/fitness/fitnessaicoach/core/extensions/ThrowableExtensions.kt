package com.fitness.fitnessaicoach.core.extensions

fun Throwable.toErrorMessage(defaultMessage: String = "Unexpected error"): String {
    return message?.takeIf { it.isNotBlank() } ?: defaultMessage
}
