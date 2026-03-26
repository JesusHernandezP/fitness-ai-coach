package com.fitness.fitnessaicoach.domain.repository

import com.fitness.fitnessaicoach.domain.model.DailyLog

interface DailyLogRepository {
    suspend fun getTodayDailyLog(): DailyLog
}
