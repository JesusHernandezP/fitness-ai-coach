package com.fitness.fitnessaicoach.presentation.bodymetrics

import com.fitness.fitnessaicoach.core.result.AppResult
import com.fitness.fitnessaicoach.domain.model.BodyMetrics
import com.fitness.fitnessaicoach.domain.model.WeightProgressPoint
import com.fitness.fitnessaicoach.domain.repository.BodyMetricsRepository
import com.fitness.fitnessaicoach.domain.usecase.GetWeightProgressUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WeightProgressViewModelTest {

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
    fun `loads weight progress on init`() = runTest {
        val progress = listOf(
            WeightProgressPoint(date = "2026-04-01", weight = 82.5),
            WeightProgressPoint(date = "2026-04-05", weight = 81.8),
            WeightProgressPoint(date = "2026-04-10", weight = 81.2)
        )
        val repository = FakeBodyMetricsRepository(progressResult = AppResult.Success(progress))

        val viewModel = WeightProgressViewModel(
            getWeightProgressUseCase = GetWeightProgressUseCase(repository)
        )

        advanceUntilIdle()

        assertEquals(progress, viewModel.uiState.value.weights)
        assertEquals(false, viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `exposes error when progress load fails`() = runTest {
        val repository = FakeBodyMetricsRepository(
            progressResult = AppResult.Error("Unable to load progress")
        )

        val viewModel = WeightProgressViewModel(
            getWeightProgressUseCase = GetWeightProgressUseCase(repository)
        )

        advanceUntilIdle()

        assertEquals(emptyList<WeightProgressPoint>(), viewModel.uiState.value.weights)
        assertEquals("Unable to load progress", viewModel.uiState.value.errorMessage)
        assertEquals(false, viewModel.uiState.value.isLoading)
    }

    private class FakeBodyMetricsRepository(
        private val progressResult: AppResult<List<WeightProgressPoint>>
    ) : BodyMetricsRepository {
        override suspend fun createBodyMetrics(bodyMetrics: BodyMetrics): AppResult<BodyMetrics> {
            error("Not used in this test")
        }

        override suspend fun getBodyMetrics(): AppResult<List<BodyMetrics>> {
            error("Not used in this test")
        }

        override suspend fun getWeightProgress(): AppResult<List<WeightProgressPoint>> = progressResult
    }
}
