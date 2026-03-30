package com.fitness.fitnessaicoach.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class DailyLogRequestDto(
    val logDate: String,
    val steps: Int? = null,
    val caloriesConsumed: Double? = null,
    val caloriesBurned: Double? = null,
    val userId: String
)
