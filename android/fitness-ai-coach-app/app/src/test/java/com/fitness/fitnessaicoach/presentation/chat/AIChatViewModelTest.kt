package com.fitness.fitnessaicoach.presentation.chat

import com.fitness.fitnessaicoach.core.result.AppResult
import com.fitness.fitnessaicoach.data.chat.AIChatRepository
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
            sendChatMessageUseCase = SendChatMessageUseCase(
                aiChatRepository = FakeAIChatRepository(
                    result = AppResult.Success("Logged your meal and updated your progress.")
                )
            )
        )

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
            sendChatMessageUseCase = SendChatMessageUseCase(
                aiChatRepository = FakeAIChatRepository(
                    result = AppResult.Error("Unable to reach the server.")
                )
            )
        )

        viewModel.sendMessage("hello")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.messages.size)
        assertEquals(AIChatRole.USER, state.messages.first().role)
        assertEquals("Unable to reach the server.", state.error)
        assertTrue(!state.isLoading)
    }

    private class FakeAIChatRepository(
        private val result: AppResult<String>
    ) : AIChatRepository {
        override suspend fun sendMessage(message: String): AppResult<String> = result
    }
}
