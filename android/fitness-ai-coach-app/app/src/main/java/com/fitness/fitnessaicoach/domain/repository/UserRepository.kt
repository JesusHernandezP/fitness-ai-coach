package com.fitness.fitnessaicoach.domain.repository

import com.fitness.fitnessaicoach.core.result.AppResult
import com.fitness.fitnessaicoach.domain.model.User

interface UserRepository {
    suspend fun getCurrentUser(): AppResult<User>
    suspend fun updateUserProfile(user: User): AppResult<User>
}
