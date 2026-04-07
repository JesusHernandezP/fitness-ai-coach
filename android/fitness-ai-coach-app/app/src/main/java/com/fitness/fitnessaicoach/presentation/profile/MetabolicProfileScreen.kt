package com.fitness.fitnessaicoach.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.graphics.SolidColor
import com.fitness.fitnessaicoach.ui.theme.BackgroundMain
import com.fitness.fitnessaicoach.ui.theme.BorderSubtle
import com.fitness.fitnessaicoach.ui.theme.CardDark
import com.fitness.fitnessaicoach.ui.theme.DividerDark
import com.fitness.fitnessaicoach.ui.theme.SurfaceDark
import com.fitness.fitnessaicoach.ui.theme.TextMuted
import com.fitness.fitnessaicoach.ui.theme.TextSecondary
import com.fitness.fitnessaicoach.ui.theme.YellowPrimary

private val sexOptions = listOf("MALE", "FEMALE")
private val activityLevelOptions = listOf("SEDENTARY", "LIGHT", "MODERATE", "ACTIVE", "VERY_ACTIVE")
private val dietTypeOptions = listOf("STANDARD", "KETO", "VEGETARIAN")
private val goalTypeOptions = listOf("LOSE_WEIGHT", "BUILD_MUSCLE", "MAINTAIN")

@Composable
fun MetabolicProfileScreen(
    viewModel: MetabolicProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var sexExpanded by remember { mutableStateOf(false) }
    var activityExpanded by remember { mutableStateOf(false) }
    var dietExpanded by remember { mutableStateOf(false) }
    var goalExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundMain)
            .statusBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Profile",
                    style = MaterialTheme.typography.headlineLarge
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Adjust the data your coach uses to personalize calories, macros and daily guidance.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        item {
            ProfileSectionCard(title = "User info") {
                AppTextField(
                    value = uiState.age,
                    onValueChange = viewModel::onAgeChanged,
                    label = "Age",
                    keyboardType = KeyboardType.Number
                )
                Spacer(modifier = Modifier.height(12.dp))
                AppTextField(
                    value = uiState.heightCm,
                    onValueChange = viewModel::onHeightChanged,
                    label = "Height cm",
                    keyboardType = KeyboardType.Decimal
                )
                Spacer(modifier = Modifier.height(12.dp))
                AppTextField(
                    value = uiState.weightKg?.toString().orEmpty(),
                    onValueChange = viewModel::onWeightChanged,
                    label = "Weight kg",
                    keyboardType = KeyboardType.Decimal
                )
            }
        }

        item {
            ProfileSectionCard(title = "Body and activity") {
                SelectionField(
                    value = uiState.sex,
                    label = "Sex",
                    buttonLabel = "Select sex",
                    expanded = sexExpanded,
                    onExpand = { sexExpanded = true },
                    onDismiss = { sexExpanded = false },
                    options = sexOptions,
                    onSelect = {
                        viewModel.onSexChanged(it)
                        sexExpanded = false
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
                SelectionField(
                    value = uiState.activityLevel,
                    label = "Activity level",
                    buttonLabel = "Select activity",
                    expanded = activityExpanded,
                    onExpand = { activityExpanded = true },
                    onDismiss = { activityExpanded = false },
                    options = activityLevelOptions,
                    onSelect = {
                        viewModel.onActivityLevelChanged(it)
                        activityExpanded = false
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
                SelectionField(
                    value = uiState.dietType,
                    label = "Diet type",
                    buttonLabel = "Select diet",
                    expanded = dietExpanded,
                    onExpand = { dietExpanded = true },
                    onDismiss = { dietExpanded = false },
                    options = dietTypeOptions,
                    onSelect = {
                        viewModel.onDietTypeChanged(it)
                        dietExpanded = false
                    }
                )
            }
        }

        item {
            ProfileSectionCard(title = "Goal") {
                SelectionField(
                    value = uiState.goalType,
                    label = "Goal",
                    buttonLabel = "Select goal",
                    expanded = goalExpanded,
                    onExpand = { goalExpanded = true },
                    onDismiss = { goalExpanded = false },
                    options = goalTypeOptions,
                    onSelect = {
                        viewModel.onGoalTypeChanged(it)
                        goalExpanded = false
                    }
                )
            }
        }

        if (uiState.targetCalories != null) {
            item {
                ProfileSectionCard(title = "Daily targets") {
                    TargetMetricCard(
                        label = "Calories target",
                        value = uiState.targetCalories?.toString().orEmpty()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SmallTargetCard(
                            label = "Protein",
                            value = uiState.targetProtein?.toString().orEmpty(),
                            modifier = Modifier.weight(1f)
                        )
                        SmallTargetCard(
                            label = "Carbs",
                            value = uiState.targetCarbs?.toString().orEmpty(),
                            modifier = Modifier.weight(1f)
                        )
                        SmallTargetCard(
                            label = "Fat",
                            value = uiState.targetFat?.toString().orEmpty(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        uiState.errorMessage?.let { message ->
            item {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        uiState.successMessage?.let { message ->
            item {
                Text(
                    text = message,
                    color = YellowPrimary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        item {
            Button(
                onClick = viewModel::saveUserProfile,
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = YellowPrimary,
                    contentColor = BackgroundMain,
                    disabledContainerColor = BorderSubtle,
                    disabledContentColor = TextMuted
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(vertical = 2.dp),
                        strokeWidth = 2.dp,
                        color = BackgroundMain
                    )
                } else {
                    Text("Save")
                }
            }
        }
    }
}

@Composable
private fun ProfileSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = CardDefaults.outlinedCardBorder().copy(
            width = 1.dp,
            brush = SolidColor(BorderSubtle)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = DividerDark)
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = SurfaceDark,
            unfocusedContainerColor = SurfaceDark,
            disabledContainerColor = SurfaceDark,
            focusedBorderColor = YellowPrimary,
            unfocusedBorderColor = BorderSubtle,
            disabledBorderColor = BorderSubtle,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedLabelColor = YellowPrimary,
            unfocusedLabelColor = TextSecondary,
            cursorColor = YellowPrimary
        )
    )
}

@Composable
private fun SelectionField(
    value: String,
    label: String,
    buttonLabel: String,
    expanded: Boolean,
    onExpand: () -> Unit,
    onDismiss: () -> Unit,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    Box {
        Column {
            AppTextField(
                value = value,
                onValueChange = { },
                label = label,
                keyboardType = KeyboardType.Text
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onExpand,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SurfaceDark,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(buttonLabel)
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismiss,
            modifier = Modifier.background(CardDark)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { onSelect(option) }
                )
            }
        }
    }
}

@Composable
private fun TargetMetricCard(
    label: String,
    value: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = CardDefaults.outlinedCardBorder().copy(
            width = 1.dp,
            brush = SolidColor(BorderSubtle)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = value,
                color = YellowPrimary,
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}

@Composable
private fun SmallTargetCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = CardDefaults.outlinedCardBorder().copy(
            width = 1.dp,
            brush = SolidColor(BorderSubtle)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = value,
                color = YellowPrimary,
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}
