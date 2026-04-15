package com.fitness.fitnessaicoach.presentation.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitness.fitnessaicoach.ui.theme.AccentYellow
import com.fitness.fitnessaicoach.ui.theme.BackgroundBase
import com.fitness.fitnessaicoach.ui.theme.CardSurface
import com.fitness.fitnessaicoach.ui.theme.CardSurfaceVariant
import com.fitness.fitnessaicoach.ui.theme.ErrorRed
import com.fitness.fitnessaicoach.ui.theme.OutlineColor
import com.fitness.fitnessaicoach.ui.theme.TextLabel
import com.fitness.fitnessaicoach.ui.theme.TextPrimary
import com.fitness.fitnessaicoach.ui.theme.TextSecondary

// ─── Option lists ────────────────────────────────────────────────────────────

private val sexOptions = listOf("MALE", "FEMALE")
private val activityLevelOptions = listOf("SEDENTARY", "LIGHT", "MODERATE", "ACTIVE", "VERY_ACTIVE")
private val goalTypeOptions = listOf("LOSE_WEIGHT", "BUILD_MUSCLE", "MAINTAIN")
private val dietTypeOptions = listOf("STANDARD", "KETO", "VEGETARIAN")

private fun sexLabel(v: String) = when (v) {
    "MALE" -> "Masculino"
    "FEMALE" -> "Femenino"
    else -> ""
}

private fun activityLabel(v: String) = when (v) {
    "SEDENTARY" -> "Sedentario"
    "LIGHT" -> "Ligero"
    "MODERATE" -> "Moderado"
    "ACTIVE" -> "Activo"
    "VERY_ACTIVE" -> "Muy activo"
    else -> ""
}

private fun goalLabel(v: String) = when (v) {
    "LOSE_WEIGHT" -> "Perder peso"
    "BUILD_MUSCLE" -> "Ganar musculo"
    "MAINTAIN" -> "Mantener"
    else -> ""
}

private fun dietLabel(v: String) = when (v) {
    "STANDARD" -> "Estandar"
    "KETO" -> "Keto"
    "VEGETARIAN" -> "Vegetariana"
    else -> ""
}

// ─── Screen ──────────────────────────────────────────────────────────────────

@Composable
fun MetabolicProfileScreen(
    viewModel: MetabolicProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item { ProfileFormCard(uiState = uiState, viewModel = viewModel) }
        item { MacrosSummaryCard(uiState = uiState) }
    }
}

// ─── Form card ───────────────────────────────────────────────────────────────

@Composable
private fun ProfileFormCard(
    uiState: MetabolicProfileUiState,
    viewModel: MetabolicProfileViewModel
) {
    ProfileCard {
        // Header
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Perfil metabolico",
                style = MaterialTheme.typography.labelMedium,
                color = AccentYellow,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Configuracion de perfil",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = "Manten tu peso, tipo de dieta y objetivo alineados entre web y Android.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }

        // Datos personales
        SectionTitle("Datos personales")
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = uiState.age,
                onValueChange = viewModel::onAgeChanged,
                label = { Text("Edad") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = profileTextFieldColors()
            )
            OutlinedTextField(
                value = uiState.heightCm,
                onValueChange = viewModel::onHeightChanged,
                label = { Text("Altura (cm)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = profileTextFieldColors()
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = uiState.weightKg?.toString().orEmpty(),
                onValueChange = { },
                label = { Text("Peso (kg)") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                readOnly = true,
                shape = RoundedCornerShape(14.dp),
                colors = profileTextFieldColors()
            )
            ProfileDropdown(
                value = uiState.sex,
                onValueChange = viewModel::onSexChanged,
                label = "Sexo",
                options = sexOptions,
                optionLabel = ::sexLabel,
                modifier = Modifier.weight(1f)
            )
        }

        // Objetivo fitness
        SectionTitle("Objetivo fitness")
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            ProfileDropdown(
                value = uiState.goalType,
                onValueChange = viewModel::onGoalTypeChanged,
                label = "Objetivo",
                options = goalTypeOptions,
                optionLabel = ::goalLabel,
                modifier = Modifier.weight(1f)
            )
            ProfileDropdown(
                value = uiState.activityLevel,
                onValueChange = viewModel::onActivityLevelChanged,
                label = "Nivel de actividad",
                options = activityLevelOptions,
                optionLabel = ::activityLabel,
                modifier = Modifier.weight(1f)
            )
        }

        ProfileDropdown(
            value = uiState.dietType,
            onValueChange = viewModel::onDietTypeChanged,
            label = "Tipo de dieta",
            options = dietTypeOptions,
            optionLabel = ::dietLabel,
            modifier = Modifier.fillMaxWidth()
        )

        // Messages
        uiState.errorMessage?.let {
            Text(text = it, color = ErrorRed, style = MaterialTheme.typography.bodyMedium)
        }
        uiState.successMessage?.let {
            Text(text = it, color = AccentYellow, style = MaterialTheme.typography.bodyMedium)
        }

        // Save button
        Button(
            onClick = viewModel::saveUserProfile,
            enabled = !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentYellow,
                contentColor = BackgroundBase
            )
        ) {
            if (uiState.isLoading) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = BackgroundBase
                    )
                    Text("Guardando...", fontWeight = FontWeight.Bold)
                }
            } else {
                Text("Guardar perfil", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ─── Macros card ─────────────────────────────────────────────────────────────

@Composable
private fun MacrosSummaryCard(uiState: MetabolicProfileUiState) {
    ProfileCard {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Macros calculados",
                style = MaterialTheme.typography.labelMedium,
                color = AccentYellow,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Objetivos diarios de ingesta",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = "Estos son tus objetivos diarios ajustados por perfil, tipo de dieta y objetivo.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MacroStat(
                    label = "Calorias",
                    value = uiState.targetCalories?.toInt()?.toString() ?: "--",
                    modifier = Modifier.weight(1f)
                )
                MacroStat(
                    label = "Proteina",
                    value = uiState.targetProtein?.toInt()?.toString()?.let { "${it}g" } ?: "--",
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MacroStat(
                    label = "Carbohidratos",
                    value = uiState.targetCarbs?.toInt()?.toString()?.let { "${it}g" } ?: "--",
                    modifier = Modifier.weight(1f)
                )
                MacroStat(
                    label = "Grasas",
                    value = uiState.targetFat?.toInt()?.toString()?.let { "${it}g" } ?: "--",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MacroStat(label: String, value: String, modifier: Modifier = Modifier) {
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
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AccentYellow
            )
        }
    }
}

// ─── Shared components ───────────────────────────────────────────────────────

@Composable
private fun ProfileCard(content: @Composable () -> Unit) {
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = AccentYellow
    )
}

@Composable
private fun ProfileDropdown(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    options: List<String>,
    optionLabel: (String) -> String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = if (value.isBlank()) "" else optionLabel(value),
            onValueChange = { },
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = profileTextFieldColors()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@Composable
private fun profileTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = CardSurfaceVariant,
    unfocusedContainerColor = CardSurfaceVariant,
    disabledContainerColor = CardSurfaceVariant.copy(alpha = 0.7f),
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    disabledTextColor = TextSecondary,
    focusedBorderColor = AccentYellow,
    unfocusedBorderColor = OutlineColor,
    focusedLabelColor = TextLabel,
    unfocusedLabelColor = TextLabel,
    cursorColor = AccentYellow
)
