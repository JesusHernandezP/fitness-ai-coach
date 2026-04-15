package com.fitness.fitnessaicoach.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitness.fitnessaicoach.core.result.AppResult
import com.fitness.fitnessaicoach.domain.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNameChanged(value: String) {
        _uiState.update { it.copy(name = value, errorMessage = null) }
    }

    fun onEmailChanged(value: String) {
        _uiState.update { it.copy(email = value, errorMessage = null) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun register() {
        val state = _uiState.value
        if (state.name.isBlank() || state.email.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Todos los campos son obligatorios.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = registerUseCase(state.name.trim(), state.email.trim(), state.password)) {
                AppResult.Loading -> Unit
                is AppResult.Success -> _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                is AppResult.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
            }
        }
    }

    fun onSuccessHandled() {
        _uiState.update { it.copy(isSuccess = false) }
    }
}
