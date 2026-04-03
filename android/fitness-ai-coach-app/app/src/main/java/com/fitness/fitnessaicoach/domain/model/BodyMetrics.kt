package com.fitness.fitnessaicoach.domain.model

data class BodyMetrics(
    val id: String? = null,
    val userId: String? = null,
    val weight: Double,
    val bodyFat: Double,
    val muscleMass: Double,
    val date: String
)
