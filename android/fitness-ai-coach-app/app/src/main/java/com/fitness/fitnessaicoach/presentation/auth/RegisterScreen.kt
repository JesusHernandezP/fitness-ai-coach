package com.fitness.fitnessaicoach.presentation.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitness.fitnessaicoach.R
import com.fitness.fitnessaicoach.ui.theme.AccentYellow
import com.fitness.fitnessaicoach.ui.theme.BackgroundBase
import com.fitness.fitnessaicoach.ui.theme.CardSurface
import com.fitness.fitnessaicoach.ui.theme.CardSurfaceVariant
import com.fitness.fitnessaicoach.ui.theme.ErrorRed
import com.fitness.fitnessaicoach.ui.theme.OutlineColor
import com.fitness.fitnessaicoach.ui.theme.TextPrimary
import com.fitness.fitnessaicoach.ui.theme.TextSecondary

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            viewModel.onSuccessHandled()
            onRegisterSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = CardSurface.copy(alpha = 0.92f)
            ),
            border = BorderStroke(1.dp, OutlineColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(36.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_fitness_ai_coach),
                    contentDescription = "Fitness AI Coach",
                    modifier = Modifier.size(90.dp)
                )

                Text(
                    text = "Fitness AI Coach",
                    style = MaterialTheme.typography.labelMedium,
                    color = AccentYellow,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Text(
                    text = "Crear cuenta",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 42.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Empieza tu progreso hoy.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::onNameChanged,
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    shape = RoundedCornerShape(16.dp),
                    colors = registerTextFieldColors()
                )

                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = viewModel::onEmailChanged,
                    label = { Text("Correo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    shape = RoundedCornerShape(16.dp),
                    colors = registerTextFieldColors()
                )

                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = viewModel::onPasswordChanged,
                    label = { Text("Contrasena") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    shape = RoundedCornerShape(16.dp),
                    colors = registerTextFieldColors()
                )

                uiState.errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = ErrorRed,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Button(
                    onClick = viewModel::register,
                    enabled = !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 54.dp),
                    shape = RoundedCornerShape(16.dp),
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
                            Text("Creando cuenta...", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text("Crear cuenta", fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedButton(
                    onClick = onBack,
                    enabled = !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 54.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, OutlineColor),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TextPrimary
                    )
                ) {
                    Text("Ya tengo cuenta", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun registerTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = CardSurfaceVariant,
    unfocusedContainerColor = CardSurfaceVariant,
    disabledContainerColor = CardSurfaceVariant.copy(alpha = 0.7f),
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    disabledTextColor = TextSecondary,
    focusedBorderColor = AccentYellow,
    unfocusedBorderColor = OutlineColor,
    focusedLabelColor = TextSecondary,
    unfocusedLabelColor = TextSecondary,
    cursorColor = AccentYellow
)
