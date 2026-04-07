package com.fitness.fitnessaicoach.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitness.fitnessaicoach.core.result.AppResult
import com.fitness.fitnessaicoach.domain.model.GoalType
import com.fitness.fitnessaicoach.domain.model.User
import com.fitness.fitnessaicoach.domain.usecase.GetCurrentUserUseCase
import com.fitness.fitnessaicoach.domain.usecase.UpdateUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MetabolicProfileUiState(
    val userId: String? = null,
    val name: String = "",
    val email: String = "",
    val weightKg: Double? = null,
    val age: String = "",
    val heightCm: String = "",
    val sex: String = "",
    val activityLevel: String = "",
    val dietType: String = "",
    val goalType: String = "",
    val targetCalories: Double? = null,
    val targetProtein: Double? = null,
    val targetCarbs: Double? = null,
    val targetFat: Double? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class MetabolicProfileViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MetabolicProfileUiState())
    val uiState: StateFlow<MetabolicProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            when (val result = getCurrentUserUseCase()) {
                AppResult.Loading -> Unit
                is AppResult.Success -> applyUser(result.data)
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
            }
        }
    }

    fun onAgeChanged(value: String) {
        _uiState.update { it.copy(age = value, errorMessage = null, successMessage = null) }
    }

    fun onHeightChanged(value: String) {
        _uiState.update { it.copy(heightCm = value, errorMessage = null, successMessage = null) }
    }

    fun onWeightChanged(value: String) {
        _uiState.update {
            it.copy(
                weightKg = value.toDoubleOrNull(),
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun onSexChanged(value: String) {
        _uiState.update { it.copy(sex = value, errorMessage = null, successMessage = null) }
    }

    fun onActivityLevelChanged(value: String) {
        _uiState.update { it.copy(activityLevel = value, errorMessage = null, successMessage = null) }
    }

    fun onGoalTypeChanged(value: String) {
        _uiState.update { it.copy(goalType = value, errorMessage = null, successMessage = null) }
    }

    fun onDietTypeChanged(value: String) {
        _uiState.update { it.copy(dietType = value, errorMessage = null, successMessage = null) }
    }

    fun saveUserProfile() {
        val currentState = _uiState.value
        val age = currentState.age.toIntOrNull()
        val heightCm = currentState.heightCm.toDoubleOrNull()
        val weightKg = currentState.weightKg

        if (age == null || age !in 15..80) {
            _uiState.update { it.copy(errorMessage = "Age must be between 15 and 80.") }
            return
        }
        if (heightCm == null || heightCm < 120.0 || heightCm > 220.0) {
            _uiState.update { it.copy(errorMessage = "Height must be between 120 and 220 cm.") }
            return
        }
        if (weightKg == null || weightKg < 30.0 || weightKg > 400.0) {
            _uiState.update { it.copy(errorMessage = "Weight must be between 30 and 400 kg.") }
            return
        }
        if (currentState.sex.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Sex is required.") }
            return
        }
        if (currentState.activityLevel.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Activity level is required.") }
            return
        }
        if (currentState.goalType.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Goal is required.") }
            return
        }
        if (currentState.dietType.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Diet type is required.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            val user = User(
                id = currentState.userId ?: "",
                name = currentState.name,
                email = currentState.email,
                age = age,
                heightCm = heightCm,
                weightKg = weightKg,
                sex = currentState.sex,
                activityLevel = currentState.activityLevel,
                dietType = currentState.dietType,
                goalType = GoalType.valueOf(currentState.goalType),
                targetCalories = currentState.targetCalories,
                targetProtein = currentState.targetProtein,
                targetCarbs = currentState.targetCarbs,
                targetFat = currentState.targetFat,
                createdAt = ""
            )

            when (val result = updateUserProfileUseCase(user)) {
                AppResult.Loading -> Unit
                is AppResult.Success -> {
                    applyUser(result.data, successMessage = "Profile saved.")
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message, successMessage = null)
                    }
                }
            }
        }
    }

    private fun applyUser(user: User, successMessage: String? = null) {
        _uiState.update {
            it.copy(
                userId = user.id,
                name = user.name,
                email = user.email,
                weightKg = user.weightKg,
                age = user.age?.toString().orEmpty(),
                heightCm = user.heightCm?.toString().orEmpty(),
                sex = user.sex.orEmpty(),
                activityLevel = user.activityLevel.orEmpty(),
                dietType = user.dietType.orEmpty(),
                goalType = user.goalType?.name.orEmpty(),
                targetCalories = user.targetCalories,
                targetProtein = user.targetProtein,
                targetCarbs = user.targetCarbs,
                targetFat = user.targetFat,
                isLoading = false,
                errorMessage = null,
                successMessage = successMessage
            )
        }
    }
}
