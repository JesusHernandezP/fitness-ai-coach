package com.fitness.fitnessaicoach.presentation.bodymetrics

import com.fitness.fitnessaicoach.core.result.AppResult
import com.fitness.fitnessaicoach.domain.model.BodyMetrics
import com.fitness.fitnessaicoach.domain.repository.BodyMetricsRepository
import com.fitness.fitnessaicoach.domain.usecase.CreateBodyMetricsUseCase
import com.fitness.fitnessaicoach.domain.usecase.GetBodyMetricsUseCase
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
class BodyMetricsViewModelTest {

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
    fun `loads body metrics history on init`() = runTest {
        val expectedMetrics = listOf(
            BodyMetrics(
                id = "metric-1",
                userId = "user-1",
                weight = 82.5,
                bodyFat = 18.2,
                muscleMass = 38.5,
                date = "2026-04-15"
            )
        )
        val repository = FakeBodyMetricsRepository(
            getResult = AppResult.Success(expectedMetrics),
            createResult = AppResult.Success(expectedMetrics.first())
        )

        val viewModel = BodyMetricsViewModel(
            createBodyMetricsUseCase = CreateBodyMetricsUseCase(repository),
            getBodyMetricsUseCase = GetBodyMetricsUseCase(repository)
        )

        advanceUntilIdle()

        assertEquals(expectedMetrics, viewModel.uiState.value.bodyMetrics)
        assertTrue(viewModel.uiState.value.errorMessage == null)
    }

    private class FakeBodyMetricsRepository(
        private val getResult: AppResult<List<BodyMetrics>>,
        private val createResult: AppResult<BodyMetrics>
    ) : BodyMetricsRepository {
        override suspend fun createBodyMetrics(bodyMetrics: BodyMetrics): AppResult<BodyMetrics> = createResult

        override suspend fun getBodyMetrics(): AppResult<List<BodyMetrics>> = getResult
    }
}
