package com.fitness.fitnessaicoach.data.remote.mapper

import com.fitness.fitnessaicoach.data.remote.dto.LoginResponseDto
import com.fitness.fitnessaicoach.domain.model.AuthToken

fun LoginResponseDto.toDomain(): AuthToken = AuthToken(token = token)
