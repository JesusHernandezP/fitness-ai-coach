package com.fitness.fitnessaicoach.presentation.bodymetrics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitness.fitnessaicoach.domain.model.WeightProgressPoint

@Composable
fun WeightProgressScreen(
    viewModel: WeightProgressViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Weight progress",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Track how your weight changes over time.",
            style = MaterialTheme.typography.bodyMedium
        )

        when {
            uiState.isLoading && uiState.weights.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.errorMessage != null -> {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = uiState.errorMessage ?: "Unable to load weight progress.",
                        color = MaterialTheme.colorScheme.error
                    )
                    Button(onClick = viewModel::loadWeightProgress) {
                        Text("Retry")
                    }
                }
            }

            uiState.weights.isEmpty() -> {
                Text(
                    text = "No weight history available yet.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            else -> {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 2.dp,
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        WeightProgressChart(
                            points = uiState.weights,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                        )
                        WeightProgressAxisLabels(points = uiState.weights)
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    uiState.weights.forEach { point ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = point.date)
                            Text(
                                text = "${point.weight} kg",
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeightProgressChart(
    points: List<WeightProgressPoint>,
    modifier: Modifier = Modifier
) {
    val lineColor = MaterialTheme.colorScheme.primary
    val axisColor = MaterialTheme.colorScheme.outline

    Canvas(modifier = modifier) {
        if (points.isEmpty()) {
            return@Canvas
        }

        val leftPadding = 24.dp.toPx()
        val bottomPadding = 24.dp.toPx()
        val topPadding = 16.dp.toPx()
        val rightPadding = 16.dp.toPx()
        val chartWidth = size.width - leftPadding - rightPadding
        val chartHeight = size.height - topPadding - bottomPadding

        drawLine(
            color = axisColor,
            start = Offset(leftPadding, topPadding),
            end = Offset(leftPadding, topPadding + chartHeight),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = axisColor,
            start = Offset(leftPadding, topPadding + chartHeight),
            end = Offset(leftPadding + chartWidth, topPadding + chartHeight),
            strokeWidth = 2.dp.toPx()
        )

        val minWeight = points.minOf { it.weight }
        val maxWeight = points.maxOf { it.weight }
        val weightRange = (maxWeight - minWeight).takeIf { it > 0.0 } ?: 1.0
        val horizontalStep = if (points.size > 1) chartWidth / (points.size - 1) else 0f

        val chartPoints = points.mapIndexed { index, point ->
            val normalizedWeight = ((point.weight - minWeight) / weightRange).toFloat()
            Offset(
                x = leftPadding + (horizontalStep * index),
                y = topPadding + chartHeight - (normalizedWeight * chartHeight)
            )
        }

        val path = Path().apply {
            moveTo(chartPoints.first().x, chartPoints.first().y)
            chartPoints.drop(1).forEach { point ->
                lineTo(point.x, point.y)
            }
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        )

        chartPoints.forEach { point ->
            drawCircle(
                color = lineColor,
                radius = 4.dp.toPx(),
                center = point
            )
        }
    }
}

@Composable
private fun WeightProgressAxisLabels(points: List<WeightProgressPoint>) {
    if (points.isEmpty()) return

    val first = points.first()
    val last = points.last()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = first.date,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (points.size > 2) {
            Text(
                text = points[points.lastIndex / 2].date,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Spacer(modifier = Modifier)
        }
        Text(
            text = last.date,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
