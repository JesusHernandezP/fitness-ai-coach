package com.fitness.fitnessaicoach.presentation.goals

import com.fitness.fitnessaicoach.core.result.AppResult
import com.fitness.fitnessaicoach.domain.model.Goal
import com.fitness.fitnessaicoach.domain.model.GoalType
import com.fitness.fitnessaicoach.domain.repository.GoalsRepository
import com.fitness.fitnessaicoach.domain.usecase.CreateGoalUseCase
import com.fitness.fitnessaicoach.domain.usecase.GetGoalsUseCase
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
class GoalsViewModelTest {

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
    fun `loads saved goals on init`() = runTest {
        val expectedGoals = listOf(
            Goal(
                id = "goal-1",
                userId = "user-1",
                goalType = GoalType.LOSE_WEIGHT,
                targetWeight = 75.0,
                targetCalories = 2000.0
            )
        )
        val repository = FakeGoalsRepository(
            getResult = AppResult.Success(expectedGoals),
            createResult = AppResult.Success(expectedGoals.first())
        )

        val viewModel = GoalsViewModel(
            createGoalUseCase = CreateGoalUseCase(repository),
            getGoalsUseCase = GetGoalsUseCase(repository)
        )

        advanceUntilIdle()

        assertEquals(expectedGoals, viewModel.uiState.value.goals)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    private class FakeGoalsRepository(
        private val getResult: AppResult<List<Goal>>,
        private val createResult: AppResult<Goal>
    ) : GoalsRepository {
        override suspend fun createGoal(goal: Goal): AppResult<Goal> = createResult

        override suspend fun getGoals(): AppResult<List<Goal>> = getResult
    }
}
