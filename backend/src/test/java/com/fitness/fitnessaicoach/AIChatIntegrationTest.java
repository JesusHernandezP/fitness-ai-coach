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
import com.fitness.fitnessaicoach.repository.BodyMetricsRepository;
import com.fitness.fitnessaicoach.repository.AIChatMessageRepository;
import com.fitness.fitnessaicoach.repository.ChatSessionRepository;
import com.fitness.fitnessaicoach.repository.DailyLogRepository;
import com.fitness.fitnessaicoach.repository.GoalRepository;
import com.fitness.fitnessaicoach.repository.MealItemRepository;
import com.fitness.fitnessaicoach.repository.MealRepository;
import com.fitness.fitnessaicoach.repository.WorkoutSessionRepository;
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

    @Test
    void swaggerSpecShouldExposeAiChatEndpoint() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['paths']['/api/ai-chat/history']").exists())
                .andExpect(jsonPath("$['paths']['/api/ai-chat/history']['get']").exists())
                .andExpect(jsonPath("$['paths']['/api/ai-chat/message']").exists())
                .andExpect(jsonPath("$['paths']['/api/ai-chat/message']['post']").exists());
    }

    @Test
    void aiChatHistoryShouldReturnChronologicalMessagesForAuthenticatedUser() throws Exception {
        when(aiTextGenerationClient.generateText(anyString()))
                .thenReturn("Assistant reply.");
        when(aiTextGenerationClient.getModelName()).thenReturn("test-model");

        UserContext user = registerAndLogin("history-endpoint");
        createGoal(user.token(), 75);
        createBodyMetric(user.token(), 79.4);
        createDailyLog(user.token(), user.userId());

        sendMessage(user.token(), "How did I do today?");
        sendMessage(user.token(), "What should I do tomorrow?");

        mockMvc.perform(get("/api/ai-chat/history")
                        .header("Authorization", "Bearer " + user.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].role").value("USER"))
                .andExpect(jsonPath("$[0].message").value("How did I do today?"))
                .andExpect(jsonPath("$[1].role").value("ASSISTANT"))
                .andExpect(jsonPath("$[1].message").value("Assistant reply."))
                .andExpect(jsonPath("$[2].role").value("USER"))
                .andExpect(jsonPath("$[2].message").value("What should I do tomorrow?"))
                .andExpect(jsonPath("$[3].role").value("ASSISTANT"))
                .andExpect(jsonPath("$[3].createdAt").isNotEmpty());
    }

    @Test
    void aiChatShouldStoreConversationAndReuseSession() throws Exception {
        when(aiTextGenerationClient.generateText(anyString()))
                .thenReturn("You are close to your calorie target. Add a short walk and keep dinner lighter.");
        when(aiTextGenerationClient.getModelName()).thenReturn("test-model");

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
                .thenReturn("Keep your calories steady and hit your step target tomorrow.");
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
        assertThat(messages.get(1).getContent()).isEqualTo("Keep your calories steady and hit your step target tomorrow.");
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
        assertThat(prompt).contains("AI: Context-aware reply.");
        assertThat(prompt).contains("USER: Message 11");
        assertThat(prompt).contains("USER MESSAGE");
        assertThat(prompt).contains("Message 12");
        assertThat(prompt).doesNotContain("USER: Message 7\r\n");
        assertThat(prompt).doesNotContain("USER: Message 12");
        assertThat(prompt.indexOf("USER: Message 9")).isLessThan(prompt.indexOf("USER: Message 11"));
    }

    @Test
    void testFoodAnalysisResponse() throws Exception {
        when(aiTextGenerationClient.generateText(anyString()))
                .thenReturn("Great choice. Chicken and rice gives you a solid base of protein and carbs for recovery. You still have room to add vegetables and another protein-rich meal later today. Would you like ideas for that next meal?");

        UserContext user = registerAndLogin("food-analysis");
        createGoal(user.token(), 82);
        createFood(user.token(), "chicken", 1.65);
        createFood(user.token(), "rice", 1.30);

        sendMessageExpectingReply(
                user.token(),
                "I ate 200g chicken and rice today",
                "Great choice. Chicken and rice gives you a solid base of protein and carbs for recovery. You still have room to add vegetables and another protein-rich meal later today. Would you like ideas for that next meal?"
        );
    }

    @Test
    void testTrainingAnalysisResponse() throws Exception {
        when(aiTextGenerationClient.generateText(anyString()))
                .thenReturn("Nice work today. Leg training plus your steps creates a strong training stimulus, so recovery and protein intake matter even more tonight. Make sure you eat a protein-rich meal and stay well hydrated.");

        UserContext user = registerAndLogin("training-analysis");

        sendMessageExpectingReply(
                user.token(),
                "I trained legs today and walked 8000 steps",
                "Nice work today. Leg training plus your steps creates a strong training stimulus, so recovery and protein intake matter even more tonight. Make sure you eat a protein-rich meal and stay well hydrated."
        );
    }

    @Test
    void testLowProteinWarning() throws Exception {
        when(aiTextGenerationClient.generateText(anyString()))
                .thenReturn("You are still quite low on protein for the day, which can make recovery harder. Try adding a protein-rich meal such as eggs, yogurt, chicken, or fish before the day ends. That will help you stay aligned with your goal.");

        UserContext user = registerAndLogin("low-protein");
        createGoal(user.token(), 80);
        createDailyLog(user.token(), user.userId());

        sendMessageExpectingReply(
                user.token(),
                "today I only ate once",
                "You are still quite low on protein for the day, which can make recovery harder. Try adding a protein-rich meal such as eggs, yogurt, chicken, or fish before the day ends. That will help you stay aligned with your goal."
        );
    }

    @Test
    void testPositiveFeedback() throws Exception {
        when(aiTextGenerationClient.generateText(anyString()))
                .thenReturn("You are doing a good job staying consistent. Your training and daily activity are supporting your goal, and that kind of consistency is what drives progress over time. Keep building on that momentum.");
        when(aiTextGenerationClient.getModelName()).thenReturn("test-model");

        UserContext user = registerAndLogin("positive-feedback");
        createGoal(user.token(), 80);
        createDailyLog(user.token(), user.userId());

        sendMessageExpectingReply(
                user.token(),
                "am I on track?",
                "You are doing a good job staying consistent. Your training and daily activity are supporting your goal, and that kind of consistency is what drives progress over time. Keep building on that momentum."
        );
    }

    @Test
    void testFollowupQuestionGeneration() throws Exception {
        when(aiTextGenerationClient.generateText(anyString()))
                .thenReturn("Fatigue can be a sign that your intake or recovery is too low for your current routine. Make sure you eat enough overall, keep protein high, and prioritize sleep tonight. Have you also noticed low energy during training?");

        UserContext user = registerAndLogin("followup-question");

        sendMessageExpectingReply(
                user.token(),
                "I feel tired lately",
                "Fatigue can be a sign that your intake or recovery is too low for your current routine. Make sure you eat enough overall, keep protein high, and prioritize sleep tonight. Have you also noticed low energy during training?"
        );
    }

    @Test
    void testCoachPromptShouldIncludeNutritionAndTrainingContext() throws Exception {
        when(aiTextGenerationClient.generateText(anyString()))
                .thenReturn("Coach reply.");

        UserContext user = registerAndLogin("prompt-context");
        createGoal(user.token(), 82);
        createFood(user.token(), "chicken", 1.65);
        createFood(user.token(), "rice", 1.30);

        sendMessage(user.token(), "I ate 200g chicken and rice today");
        reset(aiTextGenerationClient);
        when(aiTextGenerationClient.generateText(anyString())).thenReturn("Coach reply.");

        sendMessage(user.token(), "I trained legs today and walked 8000 steps");

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(aiTextGenerationClient, atLeastOnce()).generateText(promptCaptor.capture());
        String prompt = promptCaptor.getValue();

        assertThat(prompt).contains("INTERPRETATION RULES");
        assertThat(prompt).contains("FOOD ANALYSIS RULES");
        assertThat(prompt).contains("TRAINING ANALYSIS RULES");
        assertThat(prompt).contains("NUTRITION CONTEXT");
        assertThat(prompt).contains("Training today:");
        assertThat(prompt).contains("Steps today:");
        assertThat(prompt).contains("Strength training: YES");
    }

    @Test
    void aiChatShouldLogFoodWorkoutStepsWeightAndAnswerProgressQuestion() throws Exception {
        when(aiTextGenerationClient.generateText(anyString()))
                .thenReturn("Good job staying consistent today. Keep your protein high and try to finish the day with a balanced meal.");
        when(aiTextGenerationClient.getModelName()).thenReturn("test-model");

        UserContext user = registerAndLogin("log-chat");
        createGoal(user.token(), 75);
        createFood(user.token(), "eggs", 78.0);
        createFood(user.token(), "toast", 90.0);

        sendMessageExpectingReply(user.token(), "2 eggs and toast", "Good job staying consistent today. Keep your protein high and try to finish the day with a balanced meal.");
        sendMessageExpectingReply(user.token(), "pull workout 4 exercises 4x8 heavy", "Good job staying consistent today. Keep your protein high and try to finish the day with a balanced meal.");
        sendMessageExpectingReply(user.token(), "9000 steps today", "Good job staying consistent today. Keep your protein high and try to finish the day with a balanced meal.");
        sendMessageExpectingReply(user.token(), "weight 78.5 kg", "Good job staying consistent today. Keep your protein high and try to finish the day with a balanced meal.");
        sendMessageExpectingReply(user.token(), "am I in deficit?", "Good job staying consistent today. Keep your protein high and try to finish the day with a balanced meal.");

        DailyLog dailyLog = dailyLogRepository.findByUserIdAndLogDate(UUID.fromString(user.userId()), java.time.LocalDate.now())
                .orElseThrow();
        assertThat(dailyLog.getSteps()).isEqualTo(9000);
        assertThat(dailyLog.getCaloriesConsumed()).isGreaterThan(0.0);
        assertThat(dailyLog.getCaloriesBurned()).isGreaterThan(0.0);

        List<Meal> meals = mealRepository.findByDailyLogId(dailyLog.getId());
        assertThat(meals).hasSize(1);

        List<MealItem> mealItems = mealItemRepository.findAllByDailyLogId(dailyLog.getId());
        assertThat(mealItems).hasSize(2);
        assertThat(mealItems).extracting(item -> item.getFood().getName().toLowerCase()).contains("eggs", "toast");

        List<WorkoutSession> workouts = workoutSessionRepository.findByDailyLogId(dailyLog.getId());
        assertThat(workouts).hasSize(4);
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
                .thenReturn("That fits well with your routine. Keep your meals and training consistent so your progress stays on track.");
        when(aiTextGenerationClient.getModelName()).thenReturn("test-model");

        UserContext user = registerAndLogin("expanded-chat");
        createFood(user.token(), "eggs", 78.0);
        createFood(user.token(), "rice", 130.0);

        sendMessageExpectingReply(user.token(), "I ate 3 eggs and rice", "That fits well with your routine. Keep your meals and training consistent so your progress stays on track.");
        sendMessageExpectingReply(user.token(), "today I walked 8000 steps", "That fits well with your routine. Keep your meals and training consistent so your progress stays on track.");
        sendMessageExpectingReply(user.token(), "did push workout 4 exercises 4x8", "That fits well with your routine. Keep your meals and training consistent so your progress stays on track.");
        sendMessageExpectingReply(user.token(), "burned 500 calories", "That fits well with your routine. Keep your meals and training consistent so your progress stays on track.");
        sendMessageExpectingReply(user.token(), "my weight is 82kg", "That fits well with your routine. Keep your meals and training consistent so your progress stays on track.");
        sendMessageExpectingReply(user.token(), "I want to gain muscle", "That fits well with your routine. Keep your meals and training consistent so your progress stays on track.");

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
                .thenReturn("Nice session. Make sure you recover well and get enough protein today.");
        when(aiTextGenerationClient.getModelName()).thenReturn("test-model");

        UserContext user = registerAndLogin("workout-chat");

        sendMessageExpectingReply(user.token(), "pull day 4 exercises 4x8", "Nice session. Make sure you recover well and get enough protein today.");
        sendMessageExpectingReply(user.token(), "3x10 bench press", "Nice session. Make sure you recover well and get enough protein today.");
        sendMessageExpectingReply(user.token(), "did cardio 30 minutes", "Nice session. Make sure you recover well and get enough protein today.");
        sendMessageExpectingReply(user.token(), "ran 20 minutes", "Nice session. Make sure you recover well and get enough protein today.");

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
                .thenReturn("Good meal choice. Keep building meals around protein and quality carbs to support your goal.");
        when(aiTextGenerationClient.getModelName()).thenReturn("test-model");

        UserContext user = registerAndLogin("meal-quantity-chat");
        createFood(user.token(), "eggs", 78.0);
        createFood(user.token(), "rice", 130.0);
        createFood(user.token(), "bread", 265.0);

        sendMessageExpectingReply(user.token(), "2 eggs and 150g rice", "Good meal choice. Keep building meals around protein and quality carbs to support your goal.");
        sendMessageExpectingReply(user.token(), "2 slices bread", "Good meal choice. Keep building meals around protein and quality carbs to support your goal.");

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

        mockMvc.perform(post("/api/ai-chat/message")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reply").isString());
    }

    private void sendMessageExpectingReply(String token, String message, String expectedReply) throws Exception {
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
                .andExpect(jsonPath("$.reply").value(expectedReply));
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
