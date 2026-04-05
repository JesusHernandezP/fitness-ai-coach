package com.fitness.fitnessaicoach.presentation.home

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val aiCoachingState by viewModel.aiCoachingState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Button(
                onClick = onOpenBodyMetrics,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
            ) {
                Text("Body metrics")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onOpenGoals,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
            ) {
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
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "AI Coach",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    when (val coachingState = aiCoachingState) {
                        AppResult.Loading -> {
                            Column {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Generating recommendation...",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        is AppResult.Error -> {
                            Text(
                                text = "AI recommendation unavailable",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        is AppResult.Success -> {
                            if (coachingState.data.analysis.isNotBlank()) {
                                Text(
                                    text = coachingState.data.analysis,
                                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                                    textAlign = TextAlign.Start,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                            Text(
                                text = coachingState.data.advice,
                                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                                textAlign = TextAlign.Start,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}
