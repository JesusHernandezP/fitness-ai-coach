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
import androidx.compose.material3.TextButton
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
import com.fitness.fitnessaicoach.ui.theme.BackgroundMain
import com.fitness.fitnessaicoach.ui.theme.BorderSubtle
import com.fitness.fitnessaicoach.ui.theme.CardDark
import com.fitness.fitnessaicoach.ui.theme.DividerDark
import com.fitness.fitnessaicoach.ui.theme.SurfaceDark
import com.fitness.fitnessaicoach.ui.theme.TextMuted
import com.fitness.fitnessaicoach.ui.theme.TextSecondary
import com.fitness.fitnessaicoach.ui.theme.YellowPrimary

@Composable
fun AIChatScreen(
    onOpenProfile: () -> Unit,
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
        containerColor = BackgroundMain,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundMain)
                    .border(width = 1.dp, color = DividerDark)
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                uiState.error?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
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
                        placeholder = {
                            Text(
                                text = "Ask about meals, training or progress",
                                color = TextMuted
                            )
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isLoading,
                        maxLines = 4,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceDark,
                            unfocusedContainerColor = SurfaceDark,
                            disabledContainerColor = SurfaceDark,
                            focusedBorderColor = YellowPrimary,
                            unfocusedBorderColor = BorderSubtle,
                            disabledBorderColor = BorderSubtle,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            cursorColor = YellowPrimary
                        )
                    )
                    Button(
                        onClick = {
                            viewModel.sendMessage(inputText)
                            inputText = ""
                        },
                        enabled = inputText.isNotBlank() && !uiState.isLoading,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = YellowPrimary,
                            contentColor = BackgroundMain,
                            disabledContainerColor = BorderSubtle,
                            disabledContentColor = TextMuted
                        ),
                        modifier = Modifier.size(48.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Go",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundMain)
                .padding(innerPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundMain)
                    .padding(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Fitness AI Coach",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Nutrition and training guidance",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
                TextButton(onClick = onOpenProfile) {
                    Text(
                        text = "Profile",
                        color = YellowPrimary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            HorizontalDivider(color = DividerDark)

            if (uiState.messages.isEmpty() && !uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Start the conversation. Try: today I ate chicken and rice",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                        .background(
                                            color = CardDark,
                                            shape = RoundedCornerShape(18.dp)
                                        )
                                        .border(1.dp, BorderSubtle, RoundedCornerShape(18.dp))
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.height(20.dp),
                                        strokeWidth = 2.dp,
                                        color = YellowPrimary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "AI Coach is replying...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
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

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(0.82f),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Text(
                text = if (isUser) "You" else "AI Coach",
                style = MaterialTheme.typography.bodySmall,
                color = if (isUser) YellowPrimary else TextSecondary,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardDark)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            ) {
                if (!isUser) {
                    Box(
                        modifier = Modifier
                            .background(YellowPrimary)
                            .size(width = 4.dp, height = 64.dp)
                    )
                }

                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                )
            }
        }
    }
}
