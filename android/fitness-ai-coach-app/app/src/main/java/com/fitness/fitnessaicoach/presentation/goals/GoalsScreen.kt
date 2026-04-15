package com.fitness.fitnessaicoach.presentation.goals

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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitness.fitnessaicoach.domain.model.GoalType

@Composable
fun GoalsScreen(
    viewModel: GoalsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var expanded by remember { mutableStateOf(false) }

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
                    text = "Objetivos",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Define tu objetivo actual y tu meta de calorias.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        item {
            Column {
                OutlinedTextField(
                    value = uiState.goalType?.name?.replace('_', ' ') ?: "",
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Tipo de objetivo") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { expanded = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                ) {
                    Text("Seleccionar tipo de objetivo")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    GoalType.entries.forEach { goalType ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    when (goalType) {
                                        GoalType.LOSE_WEIGHT -> "Perder peso"
                                        GoalType.BUILD_MUSCLE -> "Ganar musculo"
                                        GoalType.MAINTAIN -> "Mantener"
                                    }
                                )
                            },
                            onClick = {
                                viewModel.onGoalTypeChanged(goalType)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = uiState.targetWeight,
                onValueChange = viewModel::onTargetWeightChanged,
                label = { Text("Peso objetivo (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
        }

        item {
            OutlinedTextField(
                value = uiState.targetCalories,
                onValueChange = viewModel::onTargetCaloriesChanged,
                label = { Text("Calorias objetivo") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
                onClick = viewModel::saveGoal,
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
                    Text("Guardar")
                }
            }
        }

        item {
            Text(
                text = "Objetivos guardados",
                style = MaterialTheme.typography.titleLarge
            )
        }

        if (uiState.isLoading && uiState.goals.isEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = "Cargando tus objetivos...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else if (uiState.goals.isEmpty()) {
            item {
                Text(
                    text = "Aun no hay objetivos. Agrega uno para mantener el plan enfocado.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            items(uiState.goals, key = { it.id ?: "${it.goalType}-${it.targetCalories}" }) { goal ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = when (goal.goalType) {
                            GoalType.LOSE_WEIGHT -> "Perder peso"
                            GoalType.BUILD_MUSCLE -> "Ganar musculo"
                            GoalType.MAINTAIN -> "Mantener"
                        },
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text("Calorias objetivo: ${goal.targetCalories}")
                    Text("Peso objetivo: ${goal.targetWeight?.toString() ?: "-"}")
                }
            }
        }
    }
}
