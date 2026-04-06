package com.fitness.fitnessaicoach.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class GoalsResponseDto(
    val id: String,
    val goalType: String,
    val targetWeight: Double? = null,
    val targetCalories: Double,
    val targetProtein: Double? = null,
    val targetCarbs: Double? = null,
    val targetFat: Double? = null,
    val userId: String? = null
)
