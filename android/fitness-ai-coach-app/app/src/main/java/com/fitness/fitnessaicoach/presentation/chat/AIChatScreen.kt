package com.fitness.fitnessaicoach.presentation.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitness.fitnessaicoach.ui.theme.AccentYellow
import com.fitness.fitnessaicoach.ui.theme.BackgroundBase
import com.fitness.fitnessaicoach.ui.theme.CardSurface
import com.fitness.fitnessaicoach.ui.theme.CardSurfaceVariant
import com.fitness.fitnessaicoach.ui.theme.OutlineColor
import com.fitness.fitnessaicoach.ui.theme.OutlineStrong
import com.fitness.fitnessaicoach.ui.theme.TextPrimary
import com.fitness.fitnessaicoach.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AIChatScreen(
    viewModel: AIChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var inputText by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.messages.size, uiState.isLoading) {
        val loadingOffset = if (uiState.isLoading) 1 else 0
        val targetIndex = uiState.messages.lastIndex + loadingOffset
        if (targetIndex >= 0) {
            listState.animateScrollToItem(targetIndex)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .imePadding(),
        bottomBar = {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(24.dp)
            ) {
                uiState.error?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Escribe tu mensaje...") },
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isLoading,
                        maxLines = 4,
                        shape = RoundedCornerShape(999.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OutlineColor,
                            unfocusedBorderColor = OutlineColor,
                            focusedContainerColor = CardSurfaceVariant,
                            unfocusedContainerColor = CardSurfaceVariant,
                            cursorColor = AccentYellow
                        ),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary)
                    )
                    Button(
                        onClick = {
                            viewModel.sendMessage(inputText)
                            inputText = ""
                        },
                        enabled = inputText.isNotBlank() && !uiState.isLoading,
                        modifier = Modifier.size(56.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentYellow,
                            contentColor = BackgroundBase,
                            disabledContainerColor = AccentYellow.copy(alpha = 0.7f)
                        )
                    ) {
                        Text(
                            text = "Enviar",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text(
                text = "Chat con el coach AI",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )
            HorizontalDivider(color = OutlineColor)

            if (uiState.messages.isEmpty() && !uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Inicia la conversacion. Prueba: hoy comi pollo y arroz",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        items = uiState.messages,
                        key = { message -> "${message.role}-${message.timestamp}-${message.content}" }
                    ) { message ->
                        ChatBubble(message = message)
                    }

                    if (uiState.isLoading) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth(0.7f)
                                        .background(
                                            color = CardSurface,
                                            shape = RoundedCornerShape(18.dp, 18.dp, 18.dp, 6.dp)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = OutlineStrong,
                                            shape = RoundedCornerShape(18.dp, 18.dp, 18.dp, 6.dp)
                                        )
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                ) {
                                    Text(
                                        text = "Coach AI",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = AccentYellow,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    CircularProgressIndicator(
                                        modifier = Modifier.height(20.dp),
                                        strokeWidth = 2.dp,
                                        color = AccentYellow
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: AIChatMessageItem) {
    val isUser = message.role == AIChatRole.USER
    val timestamp = formatTimestamp(message.timestamp ?: 0)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(0.7f),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Text(
                text = if (isUser) "Tu" else "Coach AI",
                style = MaterialTheme.typography.labelMedium,
                color = if (isUser) TextSecondary else AccentYellow,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary,
                modifier = Modifier
                    .background(
                        color = if (isUser) CardSurface else CardSurface,
                        shape = if (isUser)
                            RoundedCornerShape(18.dp, 18.dp, 6.dp, 18.dp)
                        else
                            RoundedCornerShape(18.dp, 18.dp, 18.dp, 6.dp)
                    )
                    .then(
                        if (isUser) {
                            Modifier.border(
                                width = 1.dp,
                                color = OutlineStrong,
                                shape = RoundedCornerShape(18.dp, 18.dp, 6.dp, 18.dp)
                            )
                        } else {
                            Modifier.border(
                                width = 1.dp,
                                color = OutlineStrong,
                                shape = RoundedCornerShape(18.dp, 18.dp, 18.dp, 6.dp)
                            ).then(
                                Modifier.border(
                                    width = 3.dp,
                                    color = AccentYellow,
                                    shape = RoundedCornerShape(18.dp, 18.dp, 18.dp, 6.dp)
                                )
                            )
                        }
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = timestamp,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    return try {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        dateFormat.format(Date(timestamp))
    } catch (e: Exception) {
        ""
    }
}
