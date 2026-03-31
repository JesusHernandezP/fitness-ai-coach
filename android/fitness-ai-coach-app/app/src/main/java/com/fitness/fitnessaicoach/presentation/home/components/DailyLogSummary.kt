package com.fitness.fitnessaicoach.presentation.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fitness.fitnessaicoach.domain.model.DailyLog

@Composable
fun DailyLogSummary(
    log: DailyLog,
    onSaveClick: () -> Unit
) {
    Column {
        Button(onClick = onSaveClick) {
            Text("Save daily log")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Today's summary")

        Spacer(modifier = Modifier.height(8.dp))

        Text("Date: ${log.date}")
        Text("Calories: ${log.caloriesConsumed}")
        Text("Calories burned: ${log.caloriesBurned}")
        Text("Steps: ${log.steps}")
    }
}
