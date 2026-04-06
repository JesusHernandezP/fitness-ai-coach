package com.fitness.fitnessaicoach.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitness.fitnessaicoach.core.result.AppResult
import com.fitness.fitnessaicoach.domain.chat.SendChatMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AIChatViewModel @Inject constructor(
    private val sendChatMessageUseCase: SendChatMessageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AIChatUiState())
    val uiState: StateFlow<AIChatUiState> = _uiState.asStateFlow()

    fun sendMessage(message: String) {
        val trimmedMessage = message.trim()
        if (trimmedMessage.isBlank() || _uiState.value.isLoading) {
            return
        }

        _uiState.update { state ->
            state.copy(
                messages = state.messages + AIChatMessageItem(
                    role = AIChatRole.USER,
                    content = trimmedMessage
                ),
                isLoading = true,
                error = null
            )
        }

        viewModelScope.launch {
            when (val result = sendChatMessageUseCase(trimmedMessage)) {
                AppResult.Loading -> Unit
                is AppResult.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            messages = state.messages + AIChatMessageItem(
                                role = AIChatRole.ASSISTANT,
                                content = result.data
                            ),
                            isLoading = false,
                            error = null
                        )
                    }
                }
                is AppResult.Error -> {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }
}
