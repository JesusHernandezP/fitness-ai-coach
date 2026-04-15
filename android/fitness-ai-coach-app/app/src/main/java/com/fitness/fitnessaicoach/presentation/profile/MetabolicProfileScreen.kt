package com.fitness.fitnessaicoach.presentation.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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

private val sexOptions = listOf("MALE", "FEMALE")
private val activityLevelOptions = listOf("SEDENTARY", "LIGHT", "MODERATE", "ACTIVE", "VERY_ACTIVE")

@Composable
fun MetabolicProfileScreen(
    viewModel: MetabolicProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var sexExpanded by remember { mutableStateOf(false) }
    var activityExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = CardSurface
                ),
                border = BorderStroke(1.dp, OutlineColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Column {
                        Text(
                            text = "Perfil metabolico",
                            style = MaterialTheme.typography.labelMedium,
                            color = AccentYellow,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Configuracion de perfil",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Manten tu peso, tipo de dieta y objetivo alineados entre web y Android.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary,
                            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Datos personales",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

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
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                                colors = profileTextFieldColors()
                            )

                            OutlinedTextField(
                                value = uiState.heightCm,
                                onValueChange = viewModel::onHeightChanged,
                                label = { Text("Altura (cm)") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
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
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                                colors = profileTextFieldColors(),
                                readOnly = true
                            )

                            Column {
                                OutlinedTextField(
                                    value = uiState.sex,
                                    onValueChange = { },
                                    readOnly = true,
                                    label = { Text("Sexo") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                                    colors = profileTextFieldColors()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { sexExpanded = true },
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 48.dp),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AccentYellow,
                                        contentColor = BackgroundBase
                                    )
                                ) {
                                    Text(
                                        "Seleccionar sexo",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = MaterialTheme.typography.bodySmall.fontSize
                                    )
                                }
                                DropdownMenu(
                                    expanded = sexExpanded,
                                    onDismissRequest = { sexExpanded = false }
                                ) {
                                    sexOptions.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(if (option == "MALE") "Masculino" else "Femenino") },
                                            onClick = {
                                                viewModel.onSexChanged(option)
                                                sexExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Objetivo fitness",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                OutlinedTextField(
                                    value = uiState.activityLevel,
                                    onValueChange = { },
                                    readOnly = true,
                                    label = { Text("Nivel de actividad") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                                    colors = profileTextFieldColors()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { activityExpanded = true },
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 48.dp),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AccentYellow,
                                        contentColor = BackgroundBase
                                    )
                                ) {
                                    Text(
                                        "Seleccionar actividad",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = MaterialTheme.typography.bodySmall.fontSize
                                    )
                                }
                                DropdownMenu(
                                    expanded = activityExpanded,
                                    onDismissRequest = { activityExpanded = false }
                                ) {
                                    activityLevelOptions.forEach { option ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    when (option) {
                                                        "SEDENTARY" -> "Sedentario"
                                                        "LIGHT" -> "Ligero"
                                                        "MODERATE" -> "Moderado"
                                                        "ACTIVE" -> "Activo"
                                                        else -> "Muy activo"
                                                    }
                                                )
                                            },
                                            onClick = {
                                                viewModel.onActivityLevelChanged(option)
                                                activityExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    uiState.errorMessage?.let { message ->
                        Text(
                            text = message,
                            color = ErrorRed,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    uiState.successMessage?.let { message ->
                        Text(
                            text = message,
                            color = AccentYellow,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Button(
                        onClick = viewModel::saveUserProfile,
                        enabled = !uiState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
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
                                Text(
                                    "Guardando...",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Text(
                                "Guardar perfil",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
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
