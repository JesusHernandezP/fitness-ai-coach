package com.fitness.fitnessaicoach.data.repository
import com.fitness.fitnessaicoach.core.extensions.toErrorMessage
import com.fitness.fitnessaicoach.core.result.AppResult
import com.fitness.fitnessaicoach.data.remote.api.CoachApi
import com.fitness.fitnessaicoach.data.remote.mapper.toDomain
import com.fitness.fitnessaicoach.domain.model.AICoachAdvice
import com.fitness.fitnessaicoach.domain.repository.AICoachRepository
import javax.inject.Inject

class AICoachRepositoryImpl @Inject constructor(
    private val coachApi: CoachApi
) : AICoachRepository {

    override suspend fun getCoaching(dailyLogId: String): AppResult<AICoachAdvice> {
        return try {
            val response = coachApi.getDailyCoaching(dailyLogId)
            AppResult.Success(response.toDomain())
        } catch (throwable: Throwable) {
            AppResult.Error(
                message = throwable.toErrorMessage("Unable to load coaching advice."),
                throwable = throwable
            )
        }
    }
}
