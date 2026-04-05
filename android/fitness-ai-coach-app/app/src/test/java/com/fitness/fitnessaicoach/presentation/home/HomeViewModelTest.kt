package com.fitness.fitnessaicoach.presentation.home

import com.fitness.fitnessaicoach.core.result.AppResult
import com.fitness.fitnessaicoach.domain.model.AICoachAdvice
import com.fitness.fitnessaicoach.domain.model.DailyLog
import com.fitness.fitnessaicoach.domain.repository.AICoachRepository
import com.fitness.fitnessaicoach.domain.repository.DailyLogRepository
import com.fitness.fitnessaicoach.domain.usecase.GetDailyCoachingUseCase
import com.fitness.fitnessaicoach.domain.usecase.GetTodayDailyLogUseCase
import com.fitness.fitnessaicoach.domain.usecase.SaveDailyLogUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loads today's daily log on init and keeps zero values`() = runTest {
        val expectedLog = DailyLog(
            id = "log-id",
            date = "2026-04-03",
            totalCalories = 0.0,
            calorieGoal = 0.0,
            protein = 0.0,
            meals = 0,
            workouts = 0,
            steps = 0,
            caloriesConsumed = 0.0,
            caloriesBurned = 0.0,
            userId = "user-id"
        )
        val repository = FakeDailyLogRepository(AppResult.Success(expectedLog))
        val coachRepository = FakeAICoachRepository(
            AppResult.Success(
                AICoachAdvice(
                    analysis = "Steps: 0",
                    advice = "Keep moving."
                )
            )
        )

        val viewModel = HomeViewModel(
            getTodayDailyLogUseCase = GetTodayDailyLogUseCase(repository),
            saveDailyLogUseCase = SaveDailyLogUseCase(repository),
            getDailyCoachingUseCase = GetDailyCoachingUseCase(coachRepository)
        )

        advanceUntilIdle()

        val state = viewModel.dailyLogState.value
        assertTrue(state is AppResult.Success)
        assertEquals(expectedLog, (state as AppResult.Success).data)
        assertTrue(viewModel.aiCoachingState.value is AppResult.Success)
    }

    @Test
    fun `refreshes ai coaching after saving daily log`() = runTest {
        val expectedLog = DailyLog(
            id = "log-id",
            date = "2026-04-03",
            totalCalories = 0.0,
            calorieGoal = 0.0,
            protein = 0.0,
            meals = 0,
            workouts = 0,
            steps = 0,
            caloriesConsumed = 0.0,
            caloriesBurned = 0.0,
            userId = "user-id"
        )
        val repository = FakeDailyLogRepository(AppResult.Success(expectedLog))
        val coachRepository = FakeAICoachRepository(
            AppResult.Success(
                AICoachAdvice(
                    analysis = "Stored analysis",
                    advice = "Stored advice"
                )
            )
        )

        val viewModel = HomeViewModel(
            getTodayDailyLogUseCase = GetTodayDailyLogUseCase(repository),
            saveDailyLogUseCase = SaveDailyLogUseCase(repository),
            getDailyCoachingUseCase = GetDailyCoachingUseCase(coachRepository)
        )

        advanceUntilIdle()
        viewModel.saveDailyLog(expectedLog.copy(steps = 1234))
        advanceUntilIdle()

        val coachingState = viewModel.aiCoachingState.value
        assertTrue(coachingState is AppResult.Success)
        assertEquals("Stored analysis", (coachingState as AppResult.Success).data.analysis)
        assertEquals(2, coachRepository.requestCount)
    }

    private class FakeDailyLogRepository(
        private val result: AppResult<DailyLog>
    ) : DailyLogRepository {
        override suspend fun getTodayDailyLog(): AppResult<DailyLog> = result

        override suspend fun saveDailyLog(dailyLog: DailyLog): DailyLog = dailyLog
    }

    private class FakeAICoachRepository(
        private val result: AppResult<AICoachAdvice>
    ) : AICoachRepository {
        var requestCount: Int = 0

        override suspend fun getCoaching(dailyLogId: String): AppResult<AICoachAdvice> = result
            .also { requestCount++ }
    }
}
