package com.fitness.fitnessaicoach.presentation.dailylog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitness.fitnessaicoach.core.result.AppResult
import com.fitness.fitnessaicoach.domain.model.DailyLog
import com.fitness.fitnessaicoach.domain.usecase.GetTodayDailyLogUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DailyLogViewModel @Inject constructor(
    private val getTodayDailyLogUseCase: GetTodayDailyLogUseCase
) : ViewModel() {

    private val _dailyLogState = MutableStateFlow<AppResult<DailyLog>>(AppResult.Loading)
    val dailyLogState: StateFlow<AppResult<DailyLog>> = _dailyLogState.asStateFlow()

    init {
        loadTodayDailyLog()
    }

    private fun loadTodayDailyLog() {
        viewModelScope.launch {
            _dailyLogState.value = AppResult.Loading
            _dailyLogState.value = getTodayDailyLogUseCase()
        }
    }
}
