package com.fitness.fitnessaicoach.domain.model

enum class GoalType {
    LOSE_WEIGHT,
    BUILD_MUSCLE,
    MAINTAIN
}

data class Goal(
    val id: String? = null,
    val userId: String? = null,
    val goalType: GoalType,
    val targetWeight: Double? = null,
    val targetCalories: Double
)
