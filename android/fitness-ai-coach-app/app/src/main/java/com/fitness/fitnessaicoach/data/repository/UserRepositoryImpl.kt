package com.fitness.fitnessaicoach.data.repository

import com.fitness.fitnessaicoach.core.extensions.toErrorMessage
import com.fitness.fitnessaicoach.core.result.AppResult
import com.fitness.fitnessaicoach.data.remote.api.UserApi
import com.fitness.fitnessaicoach.data.remote.dto.UserUpdateRequestDto
import com.fitness.fitnessaicoach.data.remote.mapper.toDomain
import com.fitness.fitnessaicoach.domain.model.User
import com.fitness.fitnessaicoach.domain.repository.UserRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userApi: UserApi
) : UserRepository {

    override suspend fun getCurrentUser(): AppResult<User> {
        return try {
            AppResult.Success(userApi.getCurrentProfile().toDomain())
        } catch (throwable: Throwable) {
            AppResult.Error(message = throwable.toErrorMessage(), throwable = throwable)
        }
    }

    override suspend fun updateUserProfile(user: User): AppResult<User> {
        return try {
            val updatedUser = userApi.updateUserProfile(
                request = UserUpdateRequestDto(
                    age = user.age ?: throw IllegalArgumentException("Age is required."),
                    heightCm = user.heightCm ?: throw IllegalArgumentException("Height is required."),
                    weightKg = user.weightKg ?: throw IllegalArgumentException("Weight is required."),
                    sex = user.sex ?: throw IllegalArgumentException("Sex is required."),
                    activityLevel = user.activityLevel ?: throw IllegalArgumentException("Activity level is required."),
                    dietType = user.dietType ?: "STANDARD",
                    goalType = user.goalType?.name
                )
            ).toDomain()

            AppResult.Success(updatedUser)
        } catch (throwable: Throwable) {
            AppResult.Error(message = throwable.toErrorMessage(), throwable = throwable)
        }
    }
}
