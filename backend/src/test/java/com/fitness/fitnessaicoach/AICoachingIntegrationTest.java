package com.fitness.fitnessaicoach;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.fitnessaicoach.ai.provider.groq.GroqClient;
import com.fitness.fitnessaicoach.domain.AIRecommendation;
import com.fitness.fitnessaicoach.dto.ai.AIAnalysisResponse;
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
    private GroqClient groqClient;

    @Test
    void swaggerSpecShouldExposeAICoachingEndpoint() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['paths']['/api/ai-coach/daily-log/{dailyLogId}']['get']").exists());
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
        when(groqClient.getCoachingResponse(anyString())).thenReturn("Great job, keep hydration steady.");

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

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isOk());

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

        return new UserContext(token);
    }

    private record UserContext(String token) {
    }
}
