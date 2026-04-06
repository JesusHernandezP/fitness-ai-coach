package com.fitness.fitnessaicoach.presentation.bodymetrics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitness.fitnessaicoach.core.result.AppResult
import com.fitness.fitnessaicoach.domain.model.WeightProgressPoint
import com.fitness.fitnessaicoach.domain.usecase.GetWeightProgressUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WeightProgressUiState(
    val weights: List<WeightProgressPoint> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class WeightProgressViewModel @Inject constructor(
    private val getWeightProgressUseCase: GetWeightProgressUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeightProgressUiState())
    val uiState: StateFlow<WeightProgressUiState> = _uiState.asStateFlow()

    init {
        loadWeightProgress()
    }

    fun loadWeightProgress() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getWeightProgressUseCase()) {
                AppResult.Loading -> Unit
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            weights = result.data,
                            isLoading = false,
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
}
