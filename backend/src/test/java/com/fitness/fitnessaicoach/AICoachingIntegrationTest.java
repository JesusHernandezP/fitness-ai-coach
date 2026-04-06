package com.fitness.fitnessaicoach;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.fitnessaicoach.ai.provider.AITextGenerationClient;
import com.fitness.fitnessaicoach.domain.AIRecommendation;
import com.fitness.fitnessaicoach.dto.ai.AIAnalysisResponse;
import com.fitness.fitnessaicoach.dto.ai.AIWeeklySummaryResponse;
import com.fitness.fitnessaicoach.repository.AIRecommendationRepository;
import com.fitness.fitnessaicoach.service.AIAnalysisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.notNullValue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AICoachingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AIRecommendationRepository aiRecommendationRepository;

    @MockBean
    private AIAnalysisService aiAnalysisService;

    @MockBean
    private AITextGenerationClient aiTextGenerationClient;

    @Test
    void swaggerSpecShouldExposeAICoachingEndpoint() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['paths']['/api/ai-coach/daily-log/{dailyLogId}']['get']").exists())
                .andExpect(jsonPath("$['paths']['/api/ai-coach/weekly-summary']['get']").exists());
    }

    @Test
    void aiCoachingShouldReturnCombinedAnalysisAndAdvice() throws Exception {
        String token = registerAndLogin().token();

        UUID dailyLogId = UUID.randomUUID();

        AIAnalysisResponse analysis = AIAnalysisResponse.builder()
                .dailyLogId(dailyLogId)
                .date(java.time.LocalDate.parse("2026-04-14"))
                .totalSteps(12000)
                .totalMeals(1)
                .totalWorkoutSessions(0)
                .totalCaloriesConsumed(BigDecimal.valueOf(1500))
                .totalCaloriesBurned(BigDecimal.valueOf(500))
                .calorieBalance(BigDecimal.valueOf(1000))
                .meals(List.of())
                .workouts(List.of())
                .build();

        when(aiAnalysisService.getDailyLogAiAnalysis(dailyLogId)).thenReturn(analysis);
        when(aiTextGenerationClient.generateText(anyString())).thenReturn("Great job, keep hydration steady.");
        when(aiTextGenerationClient.getModelName()).thenReturn("llama-test");

        mockMvc.perform(get("/api/ai-coach/daily-log/" + dailyLogId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analysis").isString())
                .andExpect(jsonPath("$.analysis").value(org.hamcrest.Matchers.containsString(dailyLogId.toString())))
                .andExpect(jsonPath("$.advice").value("Great job, keep hydration steady."));

        List<AIRecommendation> savedRecommendations = aiRecommendationRepository.findByDailyLogId(dailyLogId);
        assertThat(savedRecommendations).hasSize(1);
        assertThat(savedRecommendations.get(0).getAdvice()).isEqualTo("Great job, keep hydration steady.");
        assertThat(savedRecommendations.get(0).getAnalysisSnapshot()).contains(dailyLogId.toString());
        assertThat(savedRecommendations.get(0).getModel()).isNotBlank();
    }

    @Test
    void aiCoachingShouldReturnStoredRecommendationWhenAvailable() throws Exception {
        String token = registerAndLogin().token();
        UUID dailyLogId = UUID.randomUUID();

        aiRecommendationRepository.save(AIRecommendation.builder()
                .dailyLogId(dailyLogId)
                .analysisSnapshot("{\"dailyLogId\":\"%s\",\"summary\":\"stored\"}".formatted(dailyLogId))
                .advice("Stored coaching advice")
                .model("llama-test")
                .build());

        mockMvc.perform(get("/api/ai-coach/daily-log/" + dailyLogId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analysis").value("{\"dailyLogId\":\"%s\",\"summary\":\"stored\"}".formatted(dailyLogId)))
                .andExpect(jsonPath("$.advice").value("Stored coaching advice"));

        List<AIRecommendation> savedRecommendations = aiRecommendationRepository.findByDailyLogId(dailyLogId);
        assertThat(savedRecommendations).hasSize(1);
    }

    @Test
    void weeklySummaryShouldReturnSummaryAndRecommendationForAuthenticatedUser() throws Exception {
        UserContext user = registerAndLogin();
        createDailyLog(user.token(), user.userId(), LocalDate.parse("2026-04-10"), 1900.0, 2200.0, 8500);
        createDailyLog(user.token(), user.userId(), LocalDate.parse("2026-04-12"), 1800.0, 2100.0, 9000);
        createBodyMetric(user.token(), 82.5, "2026-04-10");
        createBodyMetric(user.token(), 81.8, "2026-04-12");

        when(aiTextGenerationClient.generateText(anyString()))
                .thenReturn("{\"summary\":\"You maintained a calorie deficit and trained consistently.\",\"recommendation\":\"Continue the current approach and keep steps above 8k.\"}");

        MvcResult result = mockMvc.perform(get("/api/ai-coach/weekly-summary")
                        .header("Authorization", "Bearer " + user.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weekStart").value("2026-04-06"))
                .andExpect(jsonPath("$.weekEnd").value("2026-04-12"))
                .andExpect(jsonPath("$.summary").isString())
                .andExpect(jsonPath("$.recommendation").isString())
                .andReturn();

        AIWeeklySummaryResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), AIWeeklySummaryResponse.class);
        assertThat(response.getSummary()).contains("calorie deficit");
        assertThat(response.getRecommendation()).contains("8k");
    }

    private void createDailyLog(String token, String userId, LocalDate date, double caloriesConsumed, double caloriesBurned, int steps) throws Exception {
        String body = """
                {
                  "logDate": "%s",
                  "steps": %s,
                  "caloriesConsumed": %s,
                  "caloriesBurned": %s,
                  "userId": "%s"
                }
                """.formatted(date, steps, caloriesConsumed, caloriesBurned, userId);

        mockMvc.perform(post("/api/daily-logs")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    private void createBodyMetric(String token, double weight, String date) throws Exception {
        String body = """
                {
                  "weight": %s,
                  "date": "%s"
                }
                """.formatted(weight, date);

        mockMvc.perform(post("/api/body-metrics")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    private UserContext registerAndLogin() throws Exception {
        String email = "ai-coach-" + UUID.randomUUID() + "@example.com";
        String password = "Passw0rd!";

        String registerBody = """
                {
                  "name": "AI Coach User",
                  "email": "%s",
                  "password": "%s",
                  "age": 33,
                  "heightCm": 180.0,
                  "weightKg": 82.0
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
                .andExpect(jsonPath("$.token", notNullValue()))
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("token")
                .asText();
        assertNotNull(token);

        return new UserContext(token, userId);
    }

    private record UserContext(String token, String userId) {
    }
}
