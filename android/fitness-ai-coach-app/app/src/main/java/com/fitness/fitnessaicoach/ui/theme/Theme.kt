package com.fitness.fitnessaicoach.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val FitnessDarkScheme = darkColorScheme(
    primary = YellowPrimary,
    onPrimary = BackgroundMain,
    background = BackgroundMain,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = CardDark,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    outlineVariant = DividerDark,
    error = ErrorColor,
    onError = TextPrimary
)

private val FitnessShapes = Shapes(
    small = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
)

@Composable
fun FitnessAiCoachTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FitnessDarkScheme,
        typography = FitnessTypography,
        shapes = FitnessShapes,
        content = content
    )
}
