package com.fitness.fitnessaicoach.presentation.chat

import com.fitness.fitnessaicoach.core.result.AppResult
import com.fitness.fitnessaicoach.data.chat.AIChatRepository
import com.fitness.fitnessaicoach.domain.chat.AIChatHistoryMessage
import com.fitness.fitnessaicoach.domain.chat.GetChatHistoryUseCase
import com.fitness.fitnessaicoach.domain.chat.SendChatMessageUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AIChatViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `sendMessage appends user and assistant messages in order`() = runTest {
        val viewModel = AIChatViewModel(
            getChatHistoryUseCase = GetChatHistoryUseCase(
                aiChatRepository = FakeAIChatRepository(
                    historyResult = AppResult.Success(emptyList()),
                    sendResult = AppResult.Success("Logged your meal and updated your progress.")
                )
            ),
            sendChatMessageUseCase = SendChatMessageUseCase(
                aiChatRepository = FakeAIChatRepository(
                    historyResult = AppResult.Success(emptyList()),
                    sendResult = AppResult.Success("Logged your meal and updated your progress.")
                )
            )
        )

        advanceUntilIdle()
        viewModel.sendMessage("today I ate chicken and rice")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.messages.size)
        assertEquals(AIChatRole.USER, state.messages[0].role)
        assertEquals("today I ate chicken and rice", state.messages[0].content)
        assertEquals(AIChatRole.ASSISTANT, state.messages[1].role)
        assertEquals("Logged your meal and updated your progress.", state.messages[1].content)
        assertFalse(state.isLoading)
        assertEquals(null, state.error)
    }

    @Test
    fun `sendMessage exposes error when repository fails`() = runTest {
        val viewModel = AIChatViewModel(
            getChatHistoryUseCase = GetChatHistoryUseCase(
                aiChatRepository = FakeAIChatRepository(
                    historyResult = AppResult.Success(emptyList()),
                    sendResult = AppResult.Error("Unable to reach the server.")
                )
            ),
            sendChatMessageUseCase = SendChatMessageUseCase(
                aiChatRepository = FakeAIChatRepository(
                    historyResult = AppResult.Success(emptyList()),
                    sendResult = AppResult.Error("Unable to reach the server.")
                )
            )
        )

        advanceUntilIdle()
        viewModel.sendMessage("hello")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.messages.size)
        assertEquals(AIChatRole.USER, state.messages.first().role)
        assertEquals("Unable to reach the server.", state.error)
        assertTrue(!state.isLoading)
    }

    @Test
    fun `loadHistory maps stored messages into ui state`() = runTest {
        val viewModel = AIChatViewModel(
            getChatHistoryUseCase = GetChatHistoryUseCase(
                aiChatRepository = FakeAIChatRepository(
                    historyResult = AppResult.Success(
                        listOf(
                            AIChatHistoryMessage(role = "USER", message = "I ate rice", createdAt = "2026-04-06T20:00:00"),
                            AIChatHistoryMessage(role = "ASSISTANT", message = "Good carb source.", createdAt = "2026-04-06T20:00:03")
                        )
                    ),
                    sendResult = AppResult.Success("ok")
                )
            ),
            sendChatMessageUseCase = SendChatMessageUseCase(
                aiChatRepository = FakeAIChatRepository(
                    historyResult = AppResult.Success(emptyList()),
                    sendResult = AppResult.Success("ok")
                )
            )
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.messages.size)
        assertEquals(AIChatRole.USER, state.messages[0].role)
        assertEquals(AIChatRole.ASSISTANT, state.messages[1].role)
        assertFalse(state.isLoading)
    }

    private class FakeAIChatRepository(
        private val historyResult: AppResult<List<AIChatHistoryMessage>>,
        private val sendResult: AppResult<String>
    ) : AIChatRepository {
        override suspend fun getHistory(): AppResult<List<AIChatHistoryMessage>> = historyResult

        override suspend fun sendMessage(message: String): AppResult<String> = sendResult
    }
}
