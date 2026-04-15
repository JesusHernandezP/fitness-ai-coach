package com.fitness.fitnessaicoach.presentation.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitness.fitnessaicoach.core.result.AppResult
import com.fitness.fitnessaicoach.presentation.home.components.DailyLogSummary
import com.fitness.fitnessaicoach.presentation.home.components.ErrorView
import com.fitness.fitnessaicoach.presentation.home.components.LoadingView
import com.fitness.fitnessaicoach.ui.theme.AccentYellow
import com.fitness.fitnessaicoach.ui.theme.CardSurface
import com.fitness.fitnessaicoach.ui.theme.OutlineColor
import com.fitness.fitnessaicoach.ui.theme.OutlineStrong
import com.fitness.fitnessaicoach.ui.theme.TextPrimary
import com.fitness.fitnessaicoach.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    onOpenProfile: () -> Unit,
    onOpenBodyMetrics: () -> Unit,
    onOpenGoals: () -> Unit,
    onOpenAIChat: () -> Unit,
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
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Navigation Cards Row 1
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                NavigationCard(
                    onClick = onOpenProfile,
                    title = "Perfil",
                    modifier = Modifier.weight(1f)
                )
                NavigationCard(
                    onClick = onOpenBodyMetrics,
                    title = "Medidas corporales",
                    modifier = Modifier.weight(1f)
                )
            }

            // Navigation Cards Row 2
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                NavigationCard(
                    onClick = onOpenGoals,
                    title = "Objetivos",
                    modifier = Modifier.weight(1f)
                )
                NavigationCard(
                    onClick = onOpenAIChat,
                    title = "Chat con el coach AI",
                    modifier = Modifier.weight(1f)
                )
            }

            // Daily Log Card
            when (val state = dailyLogState) {
                AppResult.Loading -> {
                    LoadingCard { LoadingView() }
                }

                is AppResult.Error -> {
                    LoadingCard {
                        ErrorView(
                            message = state.message,
                            onRetry = { viewModel.loadTodayDailyLog() }
                        )
                    }
                }

                is AppResult.Success -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = CardSurface
                        ),
                        border = BorderStroke(1.dp, OutlineColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Registro diario",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            DailyLogSummary(
                                log = state.data,
                                onSaveClick = { updatedLog -> viewModel.saveDailyLog(updatedLog) }
                            )
                        }
                    }
                }
            }

            // AI Coaching Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = CardSurface
                ),
                border = BorderStroke(2.dp, OutlineStrong),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Coach AI",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )

                    when (val coachingState = aiCoachingState) {
                        AppResult.Loading -> {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    color = AccentYellow,
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    text = "Generando recomendacion...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextSecondary
                                )
                            }
                        }

                        is AppResult.Error -> {
                            Text(
                                text = "Recomendacion de AI no disponible",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextSecondary
                            )
                        }

                        is AppResult.Success -> {
                            if (coachingState.data.analysis.isNotBlank()) {
                                Text(
                                    text = coachingState.data.analysis,
                                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 22.sp),
                                    color = TextSecondary,
                                    textAlign = TextAlign.Start,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Text(
                                text = coachingState.data.advice,
                                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                                color = TextPrimary,
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

@Composable
private fun NavigationCard(
    onClick: () -> Unit,
    title: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.heightIn(min = 100.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardSurface
        ),
        border = BorderStroke(1.dp, OutlineColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun LoadingCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardSurface
        ),
        border = BorderStroke(1.dp, OutlineColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Cargando...",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            content()
        }
    }
}
