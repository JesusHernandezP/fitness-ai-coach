package com.fitness.fitnessaicoach.presentation.bodymetrics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitness.fitnessaicoach.core.result.AppResult
import com.fitness.fitnessaicoach.domain.model.BodyMetrics
import com.fitness.fitnessaicoach.domain.usecase.CreateBodyMetricsUseCase
import com.fitness.fitnessaicoach.domain.usecase.GetBodyMetricsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BodyMetricsUiState(
    val weight: String = "",
    val bodyFat: String = "",
    val muscleMass: String = "",
    val date: String = LocalDate.now().toString(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val bodyMetrics: List<BodyMetrics> = emptyList()
)

@HiltViewModel
class BodyMetricsViewModel @Inject constructor(
    private val createBodyMetricsUseCase: CreateBodyMetricsUseCase,
    private val getBodyMetricsUseCase: GetBodyMetricsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BodyMetricsUiState())
    val uiState: StateFlow<BodyMetricsUiState> = _uiState.asStateFlow()

    init {
        loadBodyMetrics()
    }

    fun onWeightChanged(value: String) {
        _uiState.update { it.copy(weight = value, errorMessage = null, successMessage = null) }
    }

    fun onBodyFatChanged(value: String) {
        _uiState.update { it.copy(bodyFat = value, errorMessage = null, successMessage = null) }
    }

    fun onMuscleMassChanged(value: String) {
        _uiState.update { it.copy(muscleMass = value, errorMessage = null, successMessage = null) }
    }

    fun loadBodyMetrics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getBodyMetricsUseCase()) {
                AppResult.Loading -> Unit
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            bodyMetrics = result.data,
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

    fun saveBodyMetrics() {
        val currentState = _uiState.value
        val weight = currentState.weight.toDoubleOrNull()
        val bodyFat = currentState.bodyFat.toDoubleOrNull()
        val muscleMass = currentState.muscleMass.toDoubleOrNull()

        if (weight == null || weight <= 0.0) {
            _uiState.update { it.copy(errorMessage = "Weight must be positive.") }
            return
        }
        if (bodyFat == null || bodyFat <= 0.0) {
            _uiState.update { it.copy(errorMessage = "Body fat must be positive.") }
            return
        }
        if (muscleMass == null || muscleMass <= 0.0) {
            _uiState.update { it.copy(errorMessage = "Muscle mass must be positive.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            val bodyMetrics = BodyMetrics(
                weight = weight,
                bodyFat = bodyFat,
                muscleMass = muscleMass,
                date = currentState.date
            )

            when (val result = createBodyMetricsUseCase(bodyMetrics)) {
                AppResult.Loading -> Unit
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            weight = "",
                            bodyFat = "",
                            muscleMass = "",
                            date = LocalDate.now().toString(),
                            successMessage = "Body metrics saved.",
                            errorMessage = null
                        )
                    }
                    loadBodyMetrics()
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
