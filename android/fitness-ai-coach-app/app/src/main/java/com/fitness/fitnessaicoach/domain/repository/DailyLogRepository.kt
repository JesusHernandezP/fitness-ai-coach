package com.fitness.fitnessaicoach.domain.repository

import com.fitness.fitnessaicoach.core.result.AppResult
import com.fitness.fitnessaicoach.domain.model.DailyLog

interface DailyLogRepository {
    suspend fun getTodayDailyLog(): AppResult<DailyLog>
    suspend fun saveDailyLog(dailyLog: DailyLog): DailyLog
}
