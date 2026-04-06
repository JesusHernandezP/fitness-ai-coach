package com.fitness.fitnessaicoach;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.fitnessaicoach.ai.provider.AITextGenerationClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
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
class FinalWorkflowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AITextGenerationClient aiTextGenerationClient;

    @Test
    void shouldCompleteEndToEndUserWorkflowFromScratch() throws Exception {
        when(aiTextGenerationClient.generateText(anyString()))
                .thenAnswer(invocation -> {
                    String prompt = invocation.getArgument(0, String.class);
                    if (prompt.contains("weekly progress summary for a dashboard")) {
                        return "{\"summary\":\"You maintained a healthy calorie deficit with strong activity this week.\",\"recommendation\":\"Keep daily steps above 8k and stay consistent with training.\"}";
                    }
                    if (prompt.contains("continuous AI fitness coach in an ongoing conversation")) {
                        return "Your trend looks solid. Keep your calories controlled and maintain your current step target.";
                    }
                    return "Your daily log looks consistent. Keep your calorie balance under control and maintain your activity.";
                });
        when(aiTextGenerationClient.getModelName()).thenReturn("test-model");

        UserSession session = registerAndLogin();

        String todayLogBody = """
                {
                  "logDate": "%s",
                  "steps": 6200,
                  "caloriesConsumed": 2200.0,
                  "caloriesBurned": 350.0,
                  "userId": "%s"
                }
                """.formatted(LocalDate.now(), session.userId());

        String updatedTodayLogBody = """
                {
                  "logDate": "%s",
                  "steps": 8800,
                  "caloriesConsumed": 1950.0,
                  "caloriesBurned": 500.0,
                  "userId": "%s"
                }
                """.formatted(LocalDate.now(), session.userId());

        MvcResult createLogResult = mockMvc.perform(post("/api/daily-logs")
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(todayLogBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(session.userId()))
                .andReturn();

        String dailyLogId = objectMapper.readTree(createLogResult.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(post("/api/daily-logs")
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedTodayLogBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.steps").value(8800))
                .andExpect(jsonPath("$.caloriesConsumed").value(1950.0))
                .andExpect(jsonPath("$.caloriesBurned").value(500.0));

        mockMvc.perform(get("/api/daily-logs/today")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.steps").value(8800))
                .andExpect(jsonPath("$.caloriesConsumed").value(1950.0))
                .andExpect(jsonPath("$.caloriesBurned").value(500.0));

        String firstMetricBody = """
                {
                  "weight": 82.5,
                  "date": "%s"
                }
                """.formatted(LocalDate.now().minusDays(3));

        String secondMetricBody = """
                {
                  "weight": 81.9,
                  "date": "%s"
                }
                """.formatted(LocalDate.now());

        mockMvc.perform(post("/api/body-metrics")
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstMetricBody))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/body-metrics")
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(secondMetricBody))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/body-metrics/progress")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].weight").value(82.5))
                .andExpect(jsonPath("$[1].weight").value(81.9));

        mockMvc.perform(get("/api/ai-coach/daily-log/" + dailyLogId)
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analysis").isString())
                .andExpect(jsonPath("$.advice").isString());

        mockMvc.perform(get("/api/ai-coach/weekly-summary")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weekStart").isNotEmpty())
                .andExpect(jsonPath("$.weekEnd").isNotEmpty())
                .andExpect(jsonPath("$.summary").isString())
                .andExpect(jsonPath("$.recommendation").isString());

        mockMvc.perform(post("/api/ai-chat/message")
                        .header("Authorization", "Bearer " + session.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "How did I do this week?"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reply").isString());

        MvcResult historyResult = mockMvc.perform(get("/api/ai-chat/history")
                        .header("Authorization", "Bearer " + session.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].role").value("USER"))
                .andExpect(jsonPath("$[1].role").value("ASSISTANT"))
                .andReturn();

        JsonNode history = objectMapper.readTree(historyResult.getResponse().getContentAsString());
        assertThat(history.get(0).get("message").asText()).isEqualTo("How did I do this week?");
        assertThat(history.get(1).get("message").asText()).isNotBlank();
    }

    private UserSession registerAndLogin() throws Exception {
        String email = "workflow-" + UUID.randomUUID() + "@example.com";
        String password = "Passw0rd!";

        String registerBody = """
                {
                  "name": "Workflow User",
                  "email": "%s",
                  "password": "%s",
                  "age": 30,
                  "heightCm": 178.0,
                  "weightKg": 82.0
                }
                """.formatted(email, password);

        MvcResult registerResult = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isOk())
                .andReturn();

        String userId = objectMapper.readTree(registerResult.getResponse().getContentAsString()).get("id").asText();

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

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();
        return new UserSession(token, userId);
    }

    private record UserSession(String token, String userId) {
    }
}
