package com.fitness.fitnessaicoach.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserUpdateRequestDto(
    val age: Int,
    val heightCm: Double,
    val weightKg: Double?,
    val sex: String,
    val activityLevel: String
)
