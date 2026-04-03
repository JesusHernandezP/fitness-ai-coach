package com.fitness.fitnessaicoach.domain.usecase

import com.fitness.fitnessaicoach.domain.model.UserCredentials
import com.fitness.fitnessaicoach.domain.model.AuthToken
import com.fitness.fitnessaicoach.domain.repository.AuthRepository
import com.fitness.fitnessaicoach.core.result.AppResult
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): AppResult<AuthToken> {
        return authRepository.login(
            UserCredentials(
                email = email,
                password = password
            )
        )
    }
}
