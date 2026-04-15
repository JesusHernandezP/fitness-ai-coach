package com.fitness.fitnessaicoach.presentation.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fitness.fitnessaicoach.domain.model.DailyLog

@Composable
fun DailyLogSummary(
    log: DailyLog,
    onSaveClick: (DailyLog) -> Unit
) {
    var caloriesConsumed by remember(log.id, log.caloriesConsumed) {
        mutableStateOf((log.caloriesConsumed ?: 0.0).toString())
    }
    var caloriesBurned by remember(log.id, log.caloriesBurned) {
        mutableStateOf((log.caloriesBurned ?: 0.0).toString())
    }
    var steps by remember(log.id, log.steps) {
        mutableStateOf((log.steps ?: 0).toString())
    }

    Column {
        Text(
            text = "Resumen de hoy",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = caloriesConsumed,
            onValueChange = { caloriesConsumed = it },
            label = { Text("Calorias consumidas") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = caloriesBurned,
            onValueChange = { caloriesBurned = it },
            label = { Text("Calorias quemadas") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = steps,
            onValueChange = { steps = it },
            label = { Text("Pasos") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Fecha: ${log.date}")

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val updatedLog = log.copy(
                    caloriesConsumed = caloriesConsumed.toDoubleOrNull() ?: 0.0,
                    caloriesBurned = caloriesBurned.toDoubleOrNull() ?: 0.0,
                    steps = steps.toIntOrNull() ?: 0
                )
                onSaveClick(updatedLog)
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
        ) {
            Text("Guardar registro diario")
        }
    }
}
