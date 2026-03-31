package com.fitness.fitnessaicoach.presentation.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ErrorView(
    message: String?,
    onRetry: () -> Unit
) {
    Column {
        Text("Error loading daily log")

        Spacer(modifier = Modifier.height(8.dp))

        Text(message ?: "Unknown error")

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}
