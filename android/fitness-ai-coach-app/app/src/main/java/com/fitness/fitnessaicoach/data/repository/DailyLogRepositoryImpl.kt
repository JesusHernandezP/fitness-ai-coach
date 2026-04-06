package com.fitness.fitnessaicoach.data.repository

import com.fitness.fitnessaicoach.core.extensions.toErrorMessage
import com.fitness.fitnessaicoach.core.result.AppResult
import com.fitness.fitnessaicoach.data.remote.api.DailyLogApi
import com.fitness.fitnessaicoach.data.remote.api.UserApi
import com.fitness.fitnessaicoach.data.remote.mapper.toDomain
import com.fitness.fitnessaicoach.data.remote.mapper.toRequestDto
import com.fitness.fitnessaicoach.domain.model.DailyLog
import com.fitness.fitnessaicoach.domain.repository.DailyLogRepository
import javax.inject.Inject

class DailyLogRepositoryImpl @Inject constructor(
    private val dailyLogApi: DailyLogApi,
    private val userApi: UserApi
) : DailyLogRepository {

    override suspend fun getTodayDailyLog(): AppResult<DailyLog> {
        return try {
            AppResult.Success(dailyLogApi.getTodayDailyLog().toDomain())
        } catch (throwable: Throwable) {
            AppResult.Error(message = throwable.toErrorMessage(), throwable = throwable)
        }
    }

    override suspend fun saveDailyLog(dailyLog: DailyLog): DailyLog {
        val resolvedUserId = resolveUserId(dailyLog.userId)
        return dailyLogApi.saveDailyLog(dailyLog.toRequestDto(resolvedUserId)).toDomain()
    }

    private suspend fun resolveUserId(explicitUserId: String?): String {
        if (!explicitUserId.isNullOrBlank()) {
            return explicitUserId
        }

        return userApi.getCurrentProfile().userId
    }
}
