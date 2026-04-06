package com.fitness.fitnessaicoach.presentation.profile

import com.fitness.fitnessaicoach.core.result.AppResult
import com.fitness.fitnessaicoach.domain.model.GoalType
import com.fitness.fitnessaicoach.domain.model.User
import com.fitness.fitnessaicoach.domain.usecase.GetCurrentUserUseCase
import com.fitness.fitnessaicoach.domain.usecase.UpdateUserProfileUseCase
import com.fitness.fitnessaicoach.domain.repository.UserRepository
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
class MetabolicProfileViewModelTest {

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
    fun `saveUserProfile requires goal before saving`() = runTest {
        val repository = FakeUserRepository(
            currentUserResult = AppResult.Success(
                User(
                    id = "user-1",
                    name = "",
                    email = "",
                    age = 30,
                    heightCm = 180.0,
                    weightKg = 80.0,
                    sex = "MALE",
                    activityLevel = "ACTIVE",
                    goalType = null,
                    targetCalories = null,
                    targetProtein = null,
                    targetCarbs = null,
                    targetFat = null,
                    createdAt = ""
                )
            )
        )
        val viewModel = MetabolicProfileViewModel(
            getCurrentUserUseCase = GetCurrentUserUseCase(repository),
            updateUserProfileUseCase = UpdateUserProfileUseCase(repository)
        )

        advanceUntilIdle()
        viewModel.onAgeChanged("30")
        viewModel.onHeightChanged("180")
        viewModel.onSexChanged("MALE")
        viewModel.onActivityLevelChanged("ACTIVE")

        viewModel.saveUserProfile()

        assertEquals("Goal is required.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `saveUserProfile stores generated targets returned by backend`() = runTest {
        val repository = FakeUserRepository(
            updateResult = AppResult.Success(
                User(
                    id = "user-1",
                    name = "",
                    email = "",
                    age = 30,
                    heightCm = 180.0,
                    weightKg = 80.0,
                    sex = "MALE",
                    activityLevel = "ACTIVE",
                    goalType = GoalType.BUILD_MUSCLE,
                    targetCalories = 2900.0,
                    targetProtein = 176.0,
                    targetCarbs = 350.0,
                    targetFat = 64.0,
                    createdAt = ""
                )
            )
        )
        val viewModel = MetabolicProfileViewModel(
            getCurrentUserUseCase = GetCurrentUserUseCase(repository),
            updateUserProfileUseCase = UpdateUserProfileUseCase(repository)
        )

        advanceUntilIdle()
        viewModel.onAgeChanged("30")
        viewModel.onHeightChanged("180")
        viewModel.onSexChanged("MALE")
        viewModel.onActivityLevelChanged("ACTIVE")
        viewModel.onGoalTypeChanged("BUILD_MUSCLE")

        viewModel.saveUserProfile()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("BUILD_MUSCLE", state.goalType)
        assertEquals(2900.0, state.targetCalories)
        assertEquals(176.0, state.targetProtein)
        assertNull(state.errorMessage)
    }

    private class FakeUserRepository(
        private val currentUserResult: AppResult<User> = AppResult.Success(
            User(
                id = "user-1",
                name = "",
                email = "",
                age = 30,
                heightCm = 180.0,
                weightKg = 80.0,
                sex = "MALE",
                activityLevel = "ACTIVE",
                goalType = GoalType.MAINTAIN,
                targetCalories = 2500.0,
                targetProtein = 128.0,
                targetCarbs = 300.0,
                targetFat = 64.0,
                createdAt = ""
            )
        ),
        private val updateResult: AppResult<User> = currentUserResult
    ) : UserRepository {
        override suspend fun getCurrentUser(): AppResult<User> = currentUserResult

        override suspend fun updateUserProfile(user: User): AppResult<User> = updateResult
    }
}
