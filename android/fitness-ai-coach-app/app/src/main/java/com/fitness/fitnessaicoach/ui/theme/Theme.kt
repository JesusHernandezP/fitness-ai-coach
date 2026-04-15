package com.fitness.fitnessaicoach.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush

private val FitnessAiCoachColorScheme = darkColorScheme(
    primary = AccentYellow,
    onPrimary = BackgroundBase,
    background = BackgroundBase,
    onBackground = TextPrimary,
    surface = CardSurface,
    onSurface = TextPrimary,
    surfaceVariant = CardSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = OutlineColor,
    error = ErrorRed,
    onError = TextPrimary
)

@Composable
fun FitnessAiCoachTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FitnessAiCoachColorScheme
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(BackgroundTop, BackgroundBase)
                    )
                )
        ) {
            content()
        }
    }
}
