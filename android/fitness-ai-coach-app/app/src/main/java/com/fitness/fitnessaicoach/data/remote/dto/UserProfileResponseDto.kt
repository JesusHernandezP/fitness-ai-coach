package com.fitness.fitnessaicoach.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserProfileResponseDto(
    val userId: String,
    val age: Int? = null,
    val heightCm: Double? = null,
    val sex: String? = null,
    val activityLevel: String? = null
)
