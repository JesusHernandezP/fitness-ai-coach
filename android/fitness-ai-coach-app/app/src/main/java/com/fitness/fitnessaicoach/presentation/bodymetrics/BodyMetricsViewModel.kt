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
    val date: String = LocalDate.now().toString(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val bodyMetrics: List<BodyMetrics> = emptyList(),
    val currentWeight: Double? = null,
    val previousWeight: Double? = null,
    val weightDifference: Double? = null
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

    fun loadBodyMetrics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getBodyMetricsUseCase()) {
                AppResult.Loading -> Unit
                is AppResult.Success -> {
                    val sortedMetrics = result.data.sortedByDescending { it.date }
                    val latestMetric = sortedMetrics.getOrNull(0)
                    val previousMetric = sortedMetrics.getOrNull(1)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            bodyMetrics = sortedMetrics,
                            currentWeight = latestMetric?.weight,
                            previousWeight = previousMetric?.weight,
                            weightDifference = latestMetric?.weight?.minus(previousMetric?.weight ?: 0.0)
                                ?.takeIf { previousMetric != null },
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

        if (weight == null || weight <= 0.0) {
            _uiState.update { it.copy(errorMessage = "Weight must be positive.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            val bodyMetrics = BodyMetrics(
                weight = weight,
                date = currentState.date
            )

            when (val result = createBodyMetricsUseCase(bodyMetrics)) {
                AppResult.Loading -> Unit
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            weight = "",
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
