package com.fitness.fitnessaicoach.data.remote.mapper

import com.fitness.fitnessaicoach.data.remote.dto.WorkoutSessionResponseDto
import com.fitness.fitnessaicoach.domain.model.WorkoutSession

fun WorkoutSessionResponseDto.toDomain(): WorkoutSession = WorkoutSession(
    id = id,
    dailyLogId = dailyLogId,
    exerciseId = exerciseId,
    sets = sets,
    reps = reps,
    duration = duration,
    caloriesBurned = caloriesBurned
)
