package com.fitness.fitnessaicoach.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class GoalsRequestDto(
    val goalType: String,
    val targetWeight: Double? = null
)
