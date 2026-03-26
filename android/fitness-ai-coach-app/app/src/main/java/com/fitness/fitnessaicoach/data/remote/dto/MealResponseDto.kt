package com.fitness.fitnessaicoach.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class MealResponseDto(
    val id: String,
    val mealType: String,
    val dailyLogId: String
)
