package com.fitness.fitnessaicoach.presentation.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LoadingView() {
    Column {
        CircularProgressIndicator()

        Spacer(modifier = Modifier.height(16.dp))

        Text("Loading today's data...")
    }
}
