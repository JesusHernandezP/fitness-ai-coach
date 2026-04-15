package com.fitness.fitnessaicoach.domain.usecase

import com.fitness.fitnessaicoach.core.result.AppResult
import com.fitness.fitnessaicoach.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(name: String, email: String, password: String): AppResult<Unit> {
        return authRepository.register(name, email, password)
    }
}
