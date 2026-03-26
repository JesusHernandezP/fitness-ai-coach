package com.fitness.fitnessaicoach.domain.usecase

import com.fitness.fitnessaicoach.domain.model.LoginCredentials
import com.fitness.fitnessaicoach.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(credentials: LoginCredentials) = authRepository.login(credentials)
}
