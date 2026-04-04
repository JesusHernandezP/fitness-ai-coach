package com.fitness.fitnessaicoach.presentation.home
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitness.fitnessaicoach.core.result.AppResult
import com.fitness.fitnessaicoach.domain.model.AICoachAdvice
import com.fitness.fitnessaicoach.domain.model.DailyLog
import com.fitness.fitnessaicoach.domain.usecase.GetDailyCoachingUseCase
import com.fitness.fitnessaicoach.domain.usecase.GetTodayDailyLogUseCase
import com.fitness.fitnessaicoach.domain.usecase.SaveDailyLogUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTodayDailyLogUseCase: GetTodayDailyLogUseCase,
    private val saveDailyLogUseCase: SaveDailyLogUseCase,
    private val getDailyCoachingUseCase: GetDailyCoachingUseCase
) : ViewModel() {

    private val _dailyLogState = MutableStateFlow<AppResult<DailyLog>>(AppResult.Loading)
    val dailyLogState: StateFlow<AppResult<DailyLog>> = _dailyLogState.asStateFlow()
    private val _aiCoachingState = MutableStateFlow<AppResult<AICoachAdvice>>(AppResult.Loading)
    val aiCoachingState: StateFlow<AppResult<AICoachAdvice>> = _aiCoachingState.asStateFlow()

    init {
        loadTodayDailyLog()
    }

    fun loadTodayDailyLog() {
        viewModelScope.launch {
            _dailyLogState.value = AppResult.Loading
            val dailyLogResult = getTodayDailyLogUseCase()
            _dailyLogState.value = dailyLogResult
            when (dailyLogResult) {
                is AppResult.Success -> loadCoachingForDailyLog(dailyLogResult.data.id)
                is AppResult.Error -> _aiCoachingState.value =
                    AppResult.Error("Unable to load coaching advice.", dailyLogResult.throwable)
                AppResult.Loading -> Unit
            }
        }
    }

    fun saveDailyLog(dailyLog: DailyLog) {
        viewModelScope.launch {
            _dailyLogState.value = AppResult.Loading
            _dailyLogState.value = saveDailyLogUseCase(dailyLog)
            if (_dailyLogState.value is AppResult.Success) {
                val refreshedDailyLog = getTodayDailyLogUseCase()
                _dailyLogState.value = refreshedDailyLog
                if (refreshedDailyLog is AppResult.Success) {
                    loadCoachingForDailyLog(refreshedDailyLog.data.id)
                }
            }
        }
    }

    private suspend fun loadCoachingForDailyLog(dailyLogId: String?) {
        if (dailyLogId.isNullOrBlank()) {
            _aiCoachingState.value = AppResult.Error("Unable to load coaching advice.")
            return
        }

        _aiCoachingState.value = AppResult.Loading
        _aiCoachingState.value = getDailyCoachingUseCase(dailyLogId)
    }
}
