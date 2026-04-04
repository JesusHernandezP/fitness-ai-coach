package com.fitness.fitnessaicoach.presentation.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitness.fitnessaicoach.core.result.AppResult
import com.fitness.fitnessaicoach.presentation.home.components.DailyLogSummary
import com.fitness.fitnessaicoach.presentation.home.components.ErrorView
import com.fitness.fitnessaicoach.presentation.home.components.LoadingView

@Composable
fun HomeScreen(
    onOpenBodyMetrics: () -> Unit,
    onOpenGoals: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val dailyLogState by viewModel.dailyLogState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Button(onClick = onOpenBodyMetrics) {
            Text("Body metrics")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onOpenGoals) {
            Text("Goals")
        }
        Spacer(modifier = Modifier.height(16.dp))

        when (val state = dailyLogState) {
            AppResult.Loading -> {
                LoadingView()
            }

            is AppResult.Error -> {
                ErrorView(
                    message = state.message,
                    onRetry = { viewModel.loadTodayDailyLog() }
                )
            }

            is AppResult.Success -> {
                DailyLogSummary(
                    log = state.data,
                    onSaveClick = { updatedLog -> viewModel.saveDailyLog(updatedLog) }
                )
            }
        }
    }
}
