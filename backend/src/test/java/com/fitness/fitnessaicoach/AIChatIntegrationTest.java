package com.fitness.fitnessaicoach;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.fitnessaicoach.ai.provider.AITextGenerationClient;
import com.fitness.fitnessaicoach.domain.AIChatMessage;
import com.fitness.fitnessaicoach.domain.ChatRole;
import com.fitness.fitnessaicoach.domain.ChatSession;
import com.fitness.fitnessaicoach.domain.DailyLog;
import com.fitness.fitnessaicoach.domain.Meal;
import com.fitness.fitnessaicoach.domain.MealItem;
import com.fitness.fitnessaicoach.domain.UserGoalType;
import com.fitness.fitnessaicoach.domain.WorkoutSession;
import com.fitness.fitnessaicoach.dto.ai.AICoachingResponse;
import com.fitness.fitnessaicoach.repository.BodyMetricsRepository;
import com.fitness.fitnessaicoach.repository.AIChatMessageRepository;
import com.fitness.fitnessaicoach.repository.ChatSessionRepository;
import com.fitness.fitnessaicoach.repository.DailyLogRepository;
import com.fitness.fitnessaicoach.repository.GoalRepository;
import com.fitness.fitnessaicoach.repository.MealItemRepository;
import com.fitness.fitnessaicoach.repository.MealRepository;
import com.fitness.fitnessaicoach.repository.WorkoutSessionRepository;
import com.fitness.fitnessaicoach.service.AICoachingService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
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
    private AIChatMessageRepository aiChatMessageRepository;

    @Autowired
    private DailyLogRepository dailyLogRepository;

    @Autowired
    private MealRepository mealRepository;

    @Autowired
    private MealItemRepository mealItemRepository;

    @Autowired
    private WorkoutSessionRepository workoutSessionRepository;

    @Autowired
    private BodyMetricsRepository bodyMetricsRepository;

    @Autowired
    private GoalRepository goalRepository;

    @MockBean
    private AITextGenerationClient aiTextGenerationClient;

    @MockBean
    private AICoachingService aiCoachingService;

    @Test
    void swaggerSpecShouldExposeAiChatEndpoint() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['paths']['/api/ai/chat']").exists())
                .andExpect(jsonPath("$['paths']['/api/ai/chat']['post']").exists());
    }

    @Test
    void aiChatShouldStoreConversationAndReuseSession() throws Exception {
        when(aiTextGenerationClient.generateText(anyString()))
                .thenReturn("You are close to your calorie target. Add a short walk and keep dinner lighter.");
        when(aiTextGenerationClient.getModelName()).thenReturn("test-model");
        when(aiCoachingService.getCoaching(any()))
                .thenReturn(new AICoachingResponse(null, "You are close to your calorie target. Add a short walk and keep dinner lighter."));

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
        List<AIChatMessage> messages = aiChatMessageRepository.findBySessionIdOrderByCreatedAtAscIdAsc(session.getId());

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
                .thenReturn("Manten tus calorias estables y alcanza tu meta de pasos manana.");
        when(aiTextGenerationClient.getModelName()).thenReturn("test-model");

        UserContext user = registerAndLogin("trim-chat");
        createDailyLog(user.token(), user.userId());

        for (int index = 1; index <= 11; index++) {
            sendMessage(user.token(), "Message " + index);
        }

        ChatSession session = chatSessionRepository.findAll().stream()
                .filter(candidate -> candidate.getUser() != null && user.userId().equals(candidate.getUser().getId().toString()))
                .findFirst()
                .orElseThrow();
        List<AIChatMessage> messages = aiChatMessageRepository.findBySessionIdOrderByCreatedAtAscIdAsc(session.getId());

        assertThat(messages).hasSize(20);
        assertThat(messages.get(0).getContent()).isEqualTo("Message 2");
        assertThat(messages.get(1).getContent()).isEqualTo("Manten tus calorias estables y alcanza tu meta de pasos manana.");
        assertThat(messages.get(messages.size() - 2).getContent()).isEqualTo("Message 11");
        assertThat(messages.get(messages.size() - 1).getRole()).isEqualTo(ChatRole.AI);
    }

    @Test
    void aiChatShouldBuildChronologicalTrimmedPromptContextFromRecentUserHistory() throws Exception {
        when(aiTextGenerationClient.generateText(anyString()))
                .thenReturn("Context-aware reply.");
        when(aiTextGenerationClient.getModelName()).thenReturn("test-model");

        UserContext user = registerAndLogin("history-chat");
        createDailyLog(user.token(), user.userId());

        for (int index = 1; index <= 11; index++) {
            sendMessage(user.token(), "Message " + index);
        }

        reset(aiTextGenerationClient);
        when(aiTextGenerationClient.generateText(anyString()))
                .thenReturn("Context-aware reply.");

        sendMessage(user.token(), "Message 12");

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(aiTextGenerationClient, atLeastOnce()).generateText(promptCaptor.capture());

        String prompt = promptCaptor.getValue();
        assertThat(prompt).contains("USER: Message 9");
        assertThat(prompt).contains("ASSISTANT: Context-aware reply.");
        assertThat(prompt).contains("USER: Message 11");
        assertThat(prompt).contains("CURRENT USER MESSAGE:");
        assertThat(prompt).contains("Message 12");
        assertThat(prompt).doesNotContain("USER: Message 1" + System.lineSeparator());
        assertThat(prompt).doesNotContain("USER: Message 8");
        assertThat(prompt).doesNotContain("USER: Message 12");
        assertThat(prompt.indexOf("USER: Message 9")).isLessThan(prompt.indexOf("USER: Message 11"));
    }

    @Test
    void aiChatShouldLogFoodWorkoutStepsWeightAndAnswerProgressQuestion() throws Exception {
        when(aiTextGenerationClient.generateText(anyString()))
                .thenReturn("Estas en deficit calorico hoy. Manten la proteina alta y mantente cerca de tu meta de pasos.");
        when(aiTextGenerationClient.getModelName()).thenReturn("test-model");
        when(aiCoachingService.getCoaching(any()))
                .thenReturn(new AICoachingResponse(null, "Estas en deficit calorico hoy. Manten la proteina alta y mantente cerca de tu meta de pasos."));

        UserContext user = registerAndLogin("log-chat");
        createGoal(user.token(), 75);
        createFood(user.token(), "eggs", 78.0);
        createFood(user.token(), "toast", 90.0);

        sendMessageExpectingReply(user.token(), "2 eggs and toast", "Registre 2 alimento(s) para merienda. Cantidad total: 3.");
        sendMessageExpectingReply(user.token(), "pull workout 4 exercises 4x8 heavy", "Registre 4 ejercicios de entrenamiento para \"Pull\" con 4x8.");
        sendMessageExpectingReply(user.token(), "9000 steps today", "Registre 9000 pasos para hoy.");
        sendMessageExpectingReply(user.token(), "weight 78.5 kg", "Registre tu peso en 78.5 kg para hoy.");
        sendMessageExpectingReply(user.token(), "am I in deficit?", "Estas en deficit calorico hoy. Manten la proteina alta y mantente cerca de tu meta de pasos.");

        DailyLog dailyLog = dailyLogRepository.findByUserIdAndLogDate(UUID.fromString(user.userId()), java.time.LocalDate.now())
                .orElseThrow();
        assertThat(dailyLog.getSteps()).isEqualTo(9000);

        List<Meal> meals = mealRepository.findByDailyLogId(dailyLog.getId());
        assertThat(meals).hasSize(1);

        List<MealItem> mealItems = mealItemRepository.findAllByDailyLogId(dailyLog.getId());
        assertThat(mealItems).hasSize(2);
        assertThat(mealItems).extracting(item -> item.getFood().getName().toLowerCase()).contains("eggs", "toast");

        List<WorkoutSession> workouts = workoutSessionRepository.findByDailyLogId(dailyLog.getId());
        assertThat(workouts).hasSize(4);
        assertThat(workouts).extracting(WorkoutSession::getCaloriesBurned).allMatch(calories -> calories != null && calories > 0.0);
        assertThat(workouts).allSatisfy(workout -> {
            assertThat(workout.getSets()).isEqualTo(4);
            assertThat(workout.getReps()).isEqualTo(8);
        });
        assertThat(workouts).extracting(workout -> workout.getExercise().getName())
                .contains("Pull Exercise 1", "Pull Exercise 2", "Pull Exercise 3", "Pull Exercise 4");

        assertThat(bodyMetricsRepository.findTopByUserIdOrderByDateDescIdDesc(UUID.fromString(user.userId())))
                .get()
                .extracting(metric -> metric.getWeight())
                .isEqualTo(78.5);
    }

    @Test
    void aiChatShouldRecognizeExpandedIntentPhrases() throws Exception {
        when(aiTextGenerationClient.generateText(anyString()))
                .thenReturn("Fallback reply.");
        when(aiTextGenerationClient.getModelName()).thenReturn("test-model");

        UserContext user = registerAndLogin("expanded-chat");
        createFood(user.token(), "eggs", 78.0);
        createFood(user.token(), "rice", 130.0);

        sendMessageExpectingReply(user.token(), "I ate 3 eggs and rice", "Registre 2 alimento(s) para merienda. Cantidad total: 4.");
        sendMessageExpectingReply(user.token(), "today I walked 8000 steps", "Registre 8000 pasos para hoy.");
        sendMessageExpectingReply(user.token(), "did push workout 4 exercises 4x8", "Registre 4 ejercicios de entrenamiento para \"Push\" con 4x8.");
        sendMessageExpectingReply(user.token(), "burned 500 calories", "Registre 500 calorias quemadas para hoy.");
        sendMessageExpectingReply(user.token(), "my weight is 82kg", "Registre tu peso en 82.0 kg para hoy.");
        sendMessageExpectingReply(user.token(), "I want to gain muscle", "Estableci tu objetivo en ganar musculo.");

        DailyLog dailyLog = dailyLogRepository.findByUserIdAndLogDate(UUID.fromString(user.userId()), java.time.LocalDate.now())
                .orElseThrow();
        assertThat(dailyLog.getSteps()).isEqualTo(8000);
        assertThat(dailyLog.getCaloriesBurned()).isEqualTo(500.0);

        List<MealItem> mealItems = mealItemRepository.findAllByDailyLogId(dailyLog.getId());
        assertThat(mealItems).hasSize(2);
        assertThat(mealItems).extracting(item -> item.getQuantity()).containsExactlyInAnyOrder(3.0, 1.0);
        assertThat(mealItems).extracting(item -> item.getFood().getName().toLowerCase()).contains("eggs", "rice");

        List<WorkoutSession> workouts = workoutSessionRepository.findByDailyLogId(dailyLog.getId());
        assertThat(workouts).hasSize(4);
        assertThat(workouts).extracting(workout -> workout.getExercise().getName())
                .contains("Push Exercise 1", "Push Exercise 2", "Push Exercise 3", "Push Exercise 4");
        assertThat(workouts).allSatisfy(workout -> assertThat(workout.getCaloriesBurned()).isGreaterThan(0.0));

        assertThat(bodyMetricsRepository.findTopByUserIdOrderByDateDescIdDesc(UUID.fromString(user.userId())))
                .get()
                .extracting(metric -> metric.getWeight())
                .isEqualTo(82.0);

        assertThat(goalRepository.findTopByUserIdOrderByCreatedAtDescIdDesc(UUID.fromString(user.userId())))
                .get()
                .extracting(goal -> goal.getGoalType())
                .isEqualTo(UserGoalType.BUILD_MUSCLE);
    }

    @Test
    void aiChatShouldCreateMultipleWorkoutSessionsForExerciseCountPhrases() throws Exception {
        when(aiTextGenerationClient.generateText(anyString()))
                .thenReturn("Fallback reply.");
        when(aiTextGenerationClient.getModelName()).thenReturn("test-model");

        UserContext user = registerAndLogin("workout-chat");

        sendMessageExpectingReply(user.token(), "pull day 4 exercises 4x8", "Registre 4 ejercicios de entrenamiento para \"Pull\" con 4x8.");
        sendMessageExpectingReply(user.token(), "3x10 bench press", "Registre el entrenamiento \"Bench Press\" con 3x10.");
        sendMessageExpectingReply(user.token(), "did cardio 30 minutes", "Registre el entrenamiento \"Cardio\" con 3x10.");
        sendMessageExpectingReply(user.token(), "ran 20 minutes", "Registre el entrenamiento \"Running\" con 3x10.");

        DailyLog dailyLog = dailyLogRepository.findByUserIdAndLogDate(UUID.fromString(user.userId()), java.time.LocalDate.now())
                .orElseThrow();

        List<WorkoutSession> workouts = workoutSessionRepository.findByDailyLogId(dailyLog.getId());
        assertThat(workouts).hasSize(7);
        assertThat(workouts).filteredOn(workout -> workout.getExercise().getName().startsWith("Pull Exercise"))
                .hasSize(4);
        assertThat(workouts).filteredOn(workout -> "Bench Press".equals(workout.getExercise().getName()))
                .singleElement()
                .satisfies(workout -> {
                    assertThat(workout.getSets()).isEqualTo(3);
                    assertThat(workout.getReps()).isEqualTo(10);
                });
        assertThat(workouts).filteredOn(workout -> "Cardio".equals(workout.getExercise().getName()))
                .singleElement()
                .satisfies(workout -> assertThat(workout.getDuration()).isEqualTo(30));
        assertThat(workouts).filteredOn(workout -> "Running".equals(workout.getExercise().getName()))
                .singleElement()
                .satisfies(workout -> assertThat(workout.getDuration()).isEqualTo(20));
    }

    @Test
    void aiChatShouldExtractMealQuantitiesFromConversationalPhrases() throws Exception {
        when(aiTextGenerationClient.generateText(anyString()))
                .thenReturn("Fallback reply.");
        when(aiTextGenerationClient.getModelName()).thenReturn("test-model");

        UserContext user = registerAndLogin("meal-quantity-chat");
        createFood(user.token(), "eggs", 78.0);
        createFood(user.token(), "rice", 130.0);
        createFood(user.token(), "bread", 265.0);

        sendMessageExpectingReply(user.token(), "2 eggs and 150g rice", "Registre 2 alimento(s) para merienda. Cantidad total: 152.");
        sendMessageExpectingReply(user.token(), "2 slices bread", "Registre 1 alimento(s) para merienda. Cantidad total: 2.");

        DailyLog dailyLog = dailyLogRepository.findByUserIdAndLogDate(UUID.fromString(user.userId()), java.time.LocalDate.now())
                .orElseThrow();

        List<MealItem> mealItems = mealItemRepository.findAllByDailyLogId(dailyLog.getId());
        assertThat(mealItems).hasSize(3);
        assertThat(mealItems).filteredOn(item -> "eggs".equalsIgnoreCase(item.getFood().getName()))
                .singleElement()
                .satisfies(item -> assertThat(item.getQuantity()).isEqualTo(2.0));
        assertThat(mealItems).filteredOn(item -> "rice".equalsIgnoreCase(item.getFood().getName()))
                .singleElement()
                .satisfies(item -> assertThat(item.getQuantity()).isEqualTo(150.0));
        assertThat(mealItems).filteredOn(item -> "bread".equalsIgnoreCase(item.getFood().getName()))
                .singleElement()
                .satisfies(item -> assertThat(item.getQuantity()).isEqualTo(2.0));
    }

    private void sendMessage(String token, String message) throws Exception {
        String body = """
                {
                  "message": "%s"
                }
                """.formatted(message);

        mockMvc.perform(post("/api/ai/chat")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").isString());
    }

    private void sendMessageExpectingReply(String token, String message, String expectedReply) throws Exception {
        String body = """
                {
                  "message": "%s"
                }
                """.formatted(message);

        mockMvc.perform(post("/api/ai/chat")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value(expectedReply));
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

    private void createFood(String token, String name, double calories) throws Exception {
        String body = """
                {
                  "name": "%s",
                  "calories": %s,
                  "protein": 6.0,
                  "carbs": 12.0,
                  "fat": 4.0
                }
                """.formatted(name, calories);

        mockMvc.perform(post("/api/foods")
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

        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isCreated())
                .andReturn();

        String userId = objectMapper.readTree(registerResult.getResponse().getContentAsString())
                .get("data")
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
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("data")
                .get("token")
                .asText();

        return new UserContext(token, userId);
    }

    private record UserContext(String token, String userId) {
    }
}
