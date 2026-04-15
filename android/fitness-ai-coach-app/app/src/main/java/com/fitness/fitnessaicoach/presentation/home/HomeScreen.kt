package com.fitness.fitnessaicoach.presentation.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitness.fitnessaicoach.core.result.AppResult
import com.fitness.fitnessaicoach.domain.model.AICoachAdvice
import com.fitness.fitnessaicoach.domain.model.DailyLog
import com.fitness.fitnessaicoach.domain.model.WeightProgressPoint
import com.fitness.fitnessaicoach.ui.theme.AccentYellow
import com.fitness.fitnessaicoach.ui.theme.CardSurface
import com.fitness.fitnessaicoach.ui.theme.CardSurfaceVariant
import com.fitness.fitnessaicoach.ui.theme.OutlineColor
import com.fitness.fitnessaicoach.ui.theme.TextPrimary
import com.fitness.fitnessaicoach.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val weightProgressState by viewModel.weightProgressState.collectAsStateWithLifecycle()
    val aiCoachingState by viewModel.aiCoachingState.collectAsStateWithLifecycle()
    val dailyLogState by viewModel.dailyLogState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        WeightProgressCard(state = weightProgressState)
        WeeklySummaryCard(state = aiCoachingState)
        ActivityOverviewCard(state = dailyLogState)
    }
}

// ─── Progreso de peso ───────────────────────────────────────────────────────

@Composable
private fun WeightProgressCard(state: AppResult<List<WeightProgressPoint>>) {
    DashboardCard(title = "Progreso de peso") {
        when (state) {
            AppResult.Loading -> CardLoadingState()
            is AppResult.Error -> CardEmptyState(
                text = "No se pudo cargar el progreso de peso.",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            )
            is AppResult.Success -> {
                val points = state.data
                when {
                    points.isEmpty() -> CardEmptyState(
                        text = "Aun no tienes medidas de peso.\nRegistra la primera en tu perfil.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    )
                    points.size == 1 -> {
                        val p = points.first()
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${p.weight} kg",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentYellow
                                )
                                Text(
                                    text = p.date,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                    else -> {
                        WeightLineChart(
                            points = points,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        )
                        val last = points.last()
                        Text(
                            text = "Ultimo registro: ${last.weight} kg  ·  ${last.date}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

// ─── Resumen semanal ────────────────────────────────────────────────────────

@Composable
private fun WeeklySummaryCard(state: AppResult<AICoachAdvice>) {
    DashboardCard(title = "Resumen semanal") {
        when (state) {
            AppResult.Loading -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = AccentYellow,
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = "Generando resumen...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
            is AppResult.Error -> CardEmptyState(
                text = "Completa tu registro diario para ver tu resumen semanal."
            )
            is AppResult.Success -> {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (state.data.analysis.isNotBlank()) {
                        Text(
                            text = state.data.analysis,
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                            color = TextSecondary
                        )
                    }
                    Text(
                        text = state.data.advice,
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

// ─── Resumen de actividad ───────────────────────────────────────────────────

@Composable
private fun ActivityOverviewCard(state: AppResult<DailyLog>) {
    DashboardCard(title = "Resumen de actividad") {
        val log = (state as? AppResult.Success)?.data
        val loading = state is AppResult.Loading
        ActivityStatRow(log = log, loading = loading)
    }
}

@Composable
private fun ActivityStatRow(log: DailyLog?, loading: Boolean) {
    val dash = "—"
    val steps = when {
        loading -> dash
        log?.steps != null -> log.steps.toString()
        else -> dash
    }
    val calories = when {
        loading -> dash
        log?.caloriesConsumed != null -> "${log.caloriesConsumed.toInt()} kcal"
        else -> dash
    }
    val workouts = when {
        loading -> dash
        else -> (log?.workouts ?: 0).toString()
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ActivityStat(label = "Pasos", value = steps, modifier = Modifier.weight(1f))
        ActivityStat(label = "Calorias", value = calories, modifier = Modifier.weight(1f))
        ActivityStat(label = "Entrenos", value = workouts, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ActivityStat(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurfaceVariant),
        border = BorderStroke(1.dp, OutlineColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }
    }
}

// ─── Shared card chrome ─────────────────────────────────────────────────────

@Composable
private fun DashboardCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, OutlineColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            content()
        }
    }
}

@Composable
private fun CardLoadingState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = AccentYellow, strokeWidth = 2.dp)
    }
}

@Composable
private fun CardEmptyState(
    text: String,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

// ─── Weight line chart ──────────────────────────────────────────────────────

@Composable
private fun WeightLineChart(
    points: List<WeightProgressPoint>,
    modifier: Modifier = Modifier
) {
    val lineColor = AccentYellow
    val axisColor = OutlineColor

    Canvas(modifier = modifier) {
        val leftPad = 24.dp.toPx()
        val bottomPad = 24.dp.toPx()
        val topPad = 16.dp.toPx()
        val rightPad = 16.dp.toPx()
        val w = size.width - leftPad - rightPad
        val h = size.height - topPad - bottomPad

        drawLine(
            color = axisColor,
            start = Offset(leftPad, topPad),
            end = Offset(leftPad, topPad + h),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = axisColor,
            start = Offset(leftPad, topPad + h),
            end = Offset(leftPad + w, topPad + h),
            strokeWidth = 1.dp.toPx()
        )

        val minW = points.minOf { it.weight }
        val maxW = points.maxOf { it.weight }
        val range = (maxW - minW).takeIf { it > 0.0 } ?: 1.0
        val step = w / (points.size - 1)

        val pts = points.mapIndexed { i, p ->
            val norm = ((p.weight - minW) / range).toFloat()
            Offset(leftPad + step * i, topPad + h - norm * h)
        }

        val path = Path().apply {
            moveTo(pts.first().x, pts.first().y)
            pts.drop(1).forEach { lineTo(it.x, it.y) }
        }
        drawPath(path, color = lineColor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
        pts.forEach { drawCircle(color = lineColor, radius = 4.dp.toPx(), center = it) }
    }
}
