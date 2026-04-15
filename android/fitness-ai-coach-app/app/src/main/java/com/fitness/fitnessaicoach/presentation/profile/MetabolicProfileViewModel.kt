package com.fitness.fitnessaicoach.presentation.profile

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitness.fitnessaicoach.core.result.AppResult
import com.fitness.fitnessaicoach.domain.model.Goal
import com.fitness.fitnessaicoach.domain.model.GoalType
import com.fitness.fitnessaicoach.domain.model.User
import com.fitness.fitnessaicoach.domain.usecase.CreateGoalUseCase
import com.fitness.fitnessaicoach.domain.usecase.GetCurrentUserUseCase
import com.fitness.fitnessaicoach.domain.usecase.GetGoalsUseCase
import com.fitness.fitnessaicoach.domain.usecase.UpdateUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
    val goalType: String = "",
    val dietType: String = "",
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
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val getGoalsUseCase: GetGoalsUseCase,
    private val createGoalUseCase: CreateGoalUseCase,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    private val dietTypeKey = stringPreferencesKey("profile_diet_type")

    private val _uiState = MutableStateFlow(MetabolicProfileUiState())
    val uiState: StateFlow<MetabolicProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

            val savedDietType = dataStore.data.first()[dietTypeKey] ?: ""

            when (val userResult = getCurrentUserUseCase()) {
                AppResult.Loading -> Unit
                is AppResult.Success -> applyUser(userResult.data, dietType = savedDietType)
                is AppResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = userResult.message) }
                    return@launch
                }
            }

            when (val goalsResult = getGoalsUseCase()) {
                is AppResult.Success -> {
                    val goal = goalsResult.data.firstOrNull()
                    _uiState.update {
                        it.copy(
                            goalType = if (it.goalType.isBlank()) goal?.goalType?.name.orEmpty() else it.goalType,
                            targetCalories = goal?.targetCalories,
                            targetProtein = goal?.targetProtein,
                            targetCarbs = goal?.targetCarbs,
                            targetFat = goal?.targetFat
                        )
                    }
                }
                else -> Unit
            }
        }
    }

    fun onAgeChanged(value: String) {
        _uiState.update { it.copy(age = value, errorMessage = null, successMessage = null) }
    }

    fun onHeightChanged(value: String) {
        _uiState.update { it.copy(heightCm = value, errorMessage = null, successMessage = null) }
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

        if (age == null || age !in 15..80) {
            _uiState.update { it.copy(errorMessage = "La edad debe estar entre 15 y 80.") }
            return
        }
        if (heightCm == null || heightCm < 120.0 || heightCm > 220.0) {
            _uiState.update { it.copy(errorMessage = "La altura debe estar entre 120 y 220 cm.") }
            return
        }
        if (currentState.sex.isBlank()) {
            _uiState.update { it.copy(errorMessage = "El sexo es obligatorio.") }
            return
        }
        if (currentState.activityLevel.isBlank()) {
            _uiState.update { it.copy(errorMessage = "El nivel de actividad es obligatorio.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

            if (currentState.dietType.isNotBlank()) {
                dataStore.edit { it[dietTypeKey] = currentState.dietType }
            }

            val user = User(
                id = currentState.userId ?: "",
                name = currentState.name,
                email = currentState.email,
                age = age,
                heightCm = heightCm,
                weightKg = currentState.weightKg,
                sex = currentState.sex,
                activityLevel = currentState.activityLevel,
                createdAt = ""
            )

            when (val userResult = updateUserProfileUseCase(user)) {
                AppResult.Loading -> Unit
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = userResult.message, successMessage = null)
                    }
                }
                is AppResult.Success -> {
                    applyUser(userResult.data, successMessage = "Perfil guardado.")

                    if (currentState.goalType.isNotBlank()) {
                        val goalResult = createGoalUseCase(
                            Goal(
                                goalType = GoalType.valueOf(currentState.goalType),
                                targetWeight = currentState.weightKg,
                                targetCalories = 0.0
                            )
                        )
                        if (goalResult is AppResult.Success) {
                            val newGoal = goalResult.data
                            _uiState.update {
                                it.copy(
                                    goalType = newGoal.goalType.name,
                                    targetCalories = newGoal.targetCalories,
                                    targetProtein = newGoal.targetProtein,
                                    targetCarbs = newGoal.targetCarbs,
                                    targetFat = newGoal.targetFat
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun applyUser(user: User, successMessage: String? = null, dietType: String? = null) {
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
                dietType = dietType ?: it.dietType,
                isLoading = false,
                errorMessage = null,
                successMessage = successMessage
            )
        }
    }
}
