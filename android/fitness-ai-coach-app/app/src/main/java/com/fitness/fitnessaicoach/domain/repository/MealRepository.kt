package com.fitness.fitnessaicoach.domain.repository

import com.fitness.fitnessaicoach.domain.model.Meal

interface MealRepository {
    suspend fun getMeals(): List<Meal>
}
