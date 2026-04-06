package com.fitness.fitnessaicoach.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserProfileResponseDto(
    val userId: String,
    val age: Int? = null,
    val heightCm: Double? = null,
    val sex: String? = null,
    val activityLevel: String? = null,
    val goalType: String? = null,
    val targetCalories: Double? = null,
    val targetProtein: Double? = null,
    val targetCarbs: Double? = null,
    val targetFat: Double? = null
)
