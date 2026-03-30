package com.fitness.fitnessaicoach.presentation.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitness.fitnessaicoach.core.result.AppResult
import com.fitness.fitnessaicoach.presentation.dailylog.DailyLogViewModel

@Composable
fun HomeScreen(
    viewModel: DailyLogViewModel = hiltViewModel()
) {
    val dailyLogState by viewModel.dailyLogState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        when (val state = dailyLogState) {
            AppResult.Loading -> {
                Text(text = "Loading daily log...")
            }

            is AppResult.Error -> {
                Text(text = "Error loading daily log: ${state.message}")
            }

            is AppResult.Success -> {
                Button(
                    onClick = {
                        viewModel.saveDailyLog(
                            state.data.copy(
                                caloriesConsumed = (state.data.caloriesConsumed ?: state.data.totalCalories) + 100.0,
                                totalCalories = state.data.totalCalories + 100.0
                            )
                        )
                    }
                ) {
                    Text(text = "Save daily log")
                }

                Text(
                    text = buildString {
                        appendLine("Today's summary")
                        appendLine("Date: ${state.data.date}")
                        appendLine("Calories: ${state.data.totalCalories}")
                        appendLine("Calorie goal: ${state.data.calorieGoal}")
                        appendLine("Protein: ${state.data.protein}")
                        appendLine("Meals: ${state.data.meals}")
                        appendLine("Steps: ${state.data.steps ?: 0}")
                        append("Workouts: ${state.data.workouts}")
                    }
                )
            }
        }
    }
}
