package com.fitness.fitnessaicoach.domain.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val age: Int?,
    val heightCm: Double?,
    val weightKg: Double?,
    val sex: String? = null,
    val activityLevel: String? = null,
    val createdAt: String
)
