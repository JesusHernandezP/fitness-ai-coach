package com.fitness.fitnessaicoach.presentation.bodymetrics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun BodyMetricsScreen(
    viewModel: BodyMetricsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Body metrics",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Track your weight, body fat, and muscle mass.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        item {
            OutlinedTextField(
                value = uiState.weight,
                onValueChange = viewModel::onWeightChanged,
                label = { Text("Weight") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
        }

        item {
            OutlinedTextField(
                value = uiState.bodyFat,
                onValueChange = viewModel::onBodyFatChanged,
                label = { Text("Body fat") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
        }

        item {
            OutlinedTextField(
                value = uiState.muscleMass,
                onValueChange = viewModel::onMuscleMassChanged,
                label = { Text("Muscle mass") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
        }

        item {
            OutlinedTextField(
                value = uiState.date,
                onValueChange = {},
                label = { Text("Date") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                singleLine = true
            )
        }

        uiState.errorMessage?.let { message ->
            item {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        uiState.successMessage?.let { message ->
            item {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        item {
            Button(
                onClick = viewModel::saveBodyMetrics,
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(vertical = 2.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Save")
                }
            }
        }

        item {
            Text(
                text = "History",
                style = MaterialTheme.typography.titleLarge
            )
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Weight progress",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Current weight: ${uiState.currentWeight?.toString() ?: "-"}"
                )
                Text(
                    text = "Previous weight: ${uiState.previousWeight?.toString() ?: "-"}"
                )
                Text(
                    text = "Difference: ${uiState.weightDifference?.toString() ?: "-"}"
                )
            }
        }

        if (uiState.isLoading && uiState.bodyMetrics.isEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = "Loading your body metrics...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else if (uiState.bodyMetrics.isEmpty()) {
            item {
                Text(
                    text = "No body metrics yet. Save your first entry to start tracking progress.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            items(uiState.bodyMetrics, key = { it.id ?: it.date }) { metric ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = metric.date,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text("Weight: ${metric.weight}")
                    Text("Body fat: ${metric.bodyFat}")
                    Text("Muscle mass: ${metric.muscleMass}")
                }
            }
        }
    }
}
