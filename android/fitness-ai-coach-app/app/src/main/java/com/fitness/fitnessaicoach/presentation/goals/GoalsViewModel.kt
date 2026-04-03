package com.fitness.fitnessaicoach.presentation.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitness.fitnessaicoach.core.result.AppResult
import com.fitness.fitnessaicoach.domain.model.Goal
import com.fitness.fitnessaicoach.domain.model.GoalType
import com.fitness.fitnessaicoach.domain.usecase.CreateGoalUseCase
import com.fitness.fitnessaicoach.domain.usecase.GetGoalsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GoalsUiState(
    val goalType: GoalType? = null,
    val targetWeight: String = "",
    val targetCalories: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val goals: List<Goal> = emptyList()
)

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val createGoalUseCase: CreateGoalUseCase,
    private val getGoalsUseCase: GetGoalsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoalsUiState())
    val uiState: StateFlow<GoalsUiState> = _uiState.asStateFlow()

    init {
        loadGoals()
    }

    fun onGoalTypeChanged(goalType: GoalType) {
        _uiState.update { it.copy(goalType = goalType, errorMessage = null, successMessage = null) }
    }

    fun onTargetWeightChanged(value: String) {
        _uiState.update { it.copy(targetWeight = value, errorMessage = null, successMessage = null) }
    }

    fun onTargetCaloriesChanged(value: String) {
        _uiState.update { it.copy(targetCalories = value, errorMessage = null, successMessage = null) }
    }

    fun loadGoals() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getGoalsUseCase()) {
                AppResult.Loading -> Unit
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            goals = result.data,
                            errorMessage = null
                        )
                    }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun saveGoal() {
        val currentState = _uiState.value
        val goalType = currentState.goalType
        val targetWeight = currentState.targetWeight.takeIf { it.isNotBlank() }?.toDoubleOrNull()
        val targetCalories = currentState.targetCalories.toDoubleOrNull()

        if (goalType == null) {
            _uiState.update { it.copy(errorMessage = "Goal type is required.") }
            return
        }
        if (currentState.targetWeight.isNotBlank() && (targetWeight == null || targetWeight <= 0.0)) {
            _uiState.update { it.copy(errorMessage = "Target weight must be positive.") }
            return
        }
        if (targetCalories == null || targetCalories <= 0.0) {
            _uiState.update { it.copy(errorMessage = "Target calories must be positive.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            val goal = Goal(
                goalType = goalType,
                targetWeight = targetWeight,
                targetCalories = targetCalories
            )

            when (val result = createGoalUseCase(goal)) {
                AppResult.Loading -> Unit
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            goalType = null,
                            targetWeight = "",
                            targetCalories = "",
                            isLoading = false,
                            errorMessage = null,
                            successMessage = "Goal saved."
                        )
                    }
                    loadGoals()
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message,
                            successMessage = null
                        )
                    }
                }
            }
        }
    }
}
