package com.fitness.fitnessaicoach.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserUpdateRequestDto(
    val age: Int,
    val heightCm: Double,
    val sex: String,
    val activityLevel: String,
    val goalType: String? = null
)
