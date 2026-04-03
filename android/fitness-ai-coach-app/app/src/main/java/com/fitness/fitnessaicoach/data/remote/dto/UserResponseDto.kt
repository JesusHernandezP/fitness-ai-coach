package com.fitness.fitnessaicoach.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserResponseDto(
    val id: String,
    val name: String,
    val email: String,
    val age: Int? = null,
    val heightCm: Double? = null,
    val weightKg: Double? = null,
    val createdAt: String
)
