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
        val backendMetrics = listOf(
            BodyMetrics(
                id = "metric-2",
                userId = "user-1",
                weight = 81.0,
                date = "2026-04-14"
            ),
            BodyMetrics(
                id = "metric-1",
                userId = "user-1",
                weight = 82.5,
                date = "2026-04-15"
            )
        )
        val expectedMetrics = backendMetrics.sortedByDescending { it.date }
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
        assertEquals(82.5, viewModel.uiState.value.currentWeight)
        assertEquals(81.0, viewModel.uiState.value.previousWeight)
        assertEquals(1.5, viewModel.uiState.value.weightDifference)
        assertTrue(viewModel.uiState.value.errorMessage == null)
    }

    @Test
    fun `keeps previous weight empty when only one body metric exists`() = runTest {
        val bodyMetrics = listOf(
            BodyMetrics(
                id = "metric-1",
                userId = "user-1",
                weight = 82.5,
                date = "2026-04-15"
            )
        )
        val repository = FakeBodyMetricsRepository(
            getResult = AppResult.Success(bodyMetrics),
            createResult = AppResult.Success(bodyMetrics.first())
        )

        val viewModel = BodyMetricsViewModel(
            createBodyMetricsUseCase = CreateBodyMetricsUseCase(repository),
            getBodyMetricsUseCase = GetBodyMetricsUseCase(repository)
        )

        advanceUntilIdle()

        assertEquals(82.5, viewModel.uiState.value.currentWeight)
        assertEquals(null, viewModel.uiState.value.previousWeight)
        assertEquals(null, viewModel.uiState.value.weightDifference)
    }

    private class FakeBodyMetricsRepository(
        private val getResult: AppResult<List<BodyMetrics>>,
        private val createResult: AppResult<BodyMetrics>
    ) : BodyMetricsRepository {
        override suspend fun createBodyMetrics(bodyMetrics: BodyMetrics): AppResult<BodyMetrics> = createResult

        override suspend fun getBodyMetrics(): AppResult<List<BodyMetrics>> = getResult
    }
}
