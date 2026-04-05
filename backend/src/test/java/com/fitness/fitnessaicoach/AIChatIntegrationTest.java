package com.fitness.fitnessaicoach;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.fitnessaicoach.ai.provider.AITextGenerationClient;
import com.fitness.fitnessaicoach.domain.ChatMessage;
import com.fitness.fitnessaicoach.domain.ChatRole;
import com.fitness.fitnessaicoach.domain.ChatSession;
import com.fitness.fitnessaicoach.repository.ChatMessageRepository;
import com.fitness.fitnessaicoach.repository.ChatSessionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "app.swagger.public=true",
        "springdoc.api-docs.enabled=true",
        "springdoc.swagger-ui.enabled=true"
})
class AIChatIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @MockBean
    private AITextGenerationClient aiTextGenerationClient;

    @Test
    void swaggerSpecShouldExposeAiChatEndpoint() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['paths']['/api/ai-chat/message']").exists())
                .andExpect(jsonPath("$['paths']['/api/ai-chat/message']['post']").exists());
    }

    @Test
    void aiChatShouldStoreConversationAndReuseSession() throws Exception {
        when(aiTextGenerationClient.generateText(anyString()))
                .thenReturn("You are close to your calorie target. Add a short walk and keep dinner lighter.");

        UserContext user = registerAndLogin("ai-chat");
        createGoal(user.token(), 75);
        createBodyMetric(user.token(), 79.4);
        createDailyLog(user.token(), user.userId());

        sendMessage(user.token(), "How did I do today?");
        sendMessage(user.token(), "What should I do tomorrow?");

        List<ChatSession> sessions = chatSessionRepository.findAll().stream()
                .filter(session -> session.getUser() != null && user.userId().equals(session.getUser().getId().toString()))
                .toList();
        assertThat(sessions).hasSize(1);

        ChatSession session = sessions.get(0);
        List<ChatMessage> messages = chatMessageRepository.findBySessionIdOrderByCreatedAtAscIdAsc(session.getId());

        assertThat(messages).hasSize(4);
        assertThat(messages.get(0).getRole()).isEqualTo(ChatRole.USER);
        assertThat(messages.get(0).getContent()).isEqualTo("How did I do today?");
        assertThat(messages.get(1).getRole()).isEqualTo(ChatRole.AI);
        assertThat(messages.get(2).getRole()).isEqualTo(ChatRole.USER);
        assertThat(messages.get(2).getContent()).isEqualTo("What should I do tomorrow?");
        assertThat(messages.get(3).getRole()).isEqualTo(ChatRole.AI);
    }

    @Test
    void aiChatShouldKeepOnlyLastTwentyStoredMessagesPerSession() throws Exception {
        when(aiTextGenerationClient.generateText(anyString()))
                .thenReturn("Keep your calories steady and hit your step target tomorrow.");

        UserContext user = registerAndLogin("trim-chat");
        createDailyLog(user.token(), user.userId());

        for (int index = 1; index <= 11; index++) {
            sendMessage(user.token(), "Message " + index);
        }

        ChatSession session = chatSessionRepository.findAll().stream()
                .filter(candidate -> candidate.getUser() != null && user.userId().equals(candidate.getUser().getId().toString()))
                .findFirst()
                .orElseThrow();
        List<ChatMessage> messages = chatMessageRepository.findBySessionIdOrderByCreatedAtAscIdAsc(session.getId());

        assertThat(messages).hasSize(20);
        assertThat(messages.get(0).getContent()).isEqualTo("Message 2");
        assertThat(messages.get(1).getContent()).isEqualTo("Keep your calories steady and hit your step target tomorrow.");
        assertThat(messages.get(messages.size() - 2).getContent()).isEqualTo("Message 11");
        assertThat(messages.get(messages.size() - 1).getRole()).isEqualTo(ChatRole.AI);
    }

    private void sendMessage(String token, String message) throws Exception {
        String body = """
                {
                  "message": "%s"
                }
                """.formatted(message);

        mockMvc.perform(post("/api/ai-chat/message")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reply").isString());
    }

    private void createGoal(String token, double targetWeight) throws Exception {
        String body = """
                {
                  "goalType": "LOSE_WEIGHT",
                  "targetWeight": %s
                }
                """.formatted(targetWeight);

        mockMvc.perform(post("/api/goals")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    private void createBodyMetric(String token, double weight) throws Exception {
        String body = """
                {
                  "weight": %s,
                  "date": "2026-04-15"
                }
                """.formatted(weight);

        mockMvc.perform(post("/api/body-metrics")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    private void createDailyLog(String token, String userId) throws Exception {
        String body = """
                {
                  "logDate": "2026-04-15",
                  "steps": 6400,
                  "caloriesConsumed": 2100.0,
                  "caloriesBurned": 450.0,
                  "userId": "%s"
                }
                """.formatted(userId);

        mockMvc.perform(post("/api/daily-logs")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    private UserContext registerAndLogin(String prefix) throws Exception {
        String email = prefix + "-" + UUID.randomUUID() + "@example.com";
        String password = "Passw0rd!";

        String registerBody = """
                {
                  "name": "AI Chat User",
                  "email": "%s",
                  "password": "%s",
                  "age": 31,
                  "heightCm": 178.0,
                  "weightKg": 80.0
                }
                """.formatted(email, password);

        MvcResult registerResult = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isOk())
                .andReturn();

        String userId = objectMapper.readTree(registerResult.getResponse().getContentAsString())
                .get("id")
                .asText();

        String loginBody = """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(email, password);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("token")
                .asText();

        return new UserContext(token, userId);
    }

    private record UserContext(String token, String userId) {
    }
}
