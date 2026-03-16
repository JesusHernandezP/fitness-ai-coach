package com.fitness.fitnessaicoach;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.hamcrest.Matchers.nullValue;
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
public class AIAnalysisIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void swaggerSpecShouldExposeAiAnalysisEndpoint() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['paths']['/api/ai-analysis/daily-log/{dailyLogId}']['get']").exists());
    }

    @Test
    void aiAnalysisShouldReturnStructuredPayloadWhenDataExists() throws Exception {
        UserContext user = registerAndLogin();
        String token = user.token();

        UUID dailyLogId = createDailyLog(token, user.userId());
        UUID mealId = createMeal(token, dailyLogId, "BREAKFAST");
        UUID lunchMealId = createMeal(token, dailyLogId, "LUNCH");
        UUID chickenFoodId = createFood(token, "Chicken", 120.0);
        UUID oatsFoodId = createFood(token, "Oats", 150.0);

        createMealItem(token, mealId, chickenFoodId, 1);
        createMealItem(token, lunchMealId, oatsFoodId, 2);

        UUID exerciseId = createExercise(token);
        createWorkoutSession(token, dailyLogId, exerciseId, 180.0);

        createGoal(token, user.userId());
        createBodyMetric(token, user.userId(), 81.5, 20.2, 36.4, "2026-04-14");
        createBodyMetric(token, user.userId(), 80.7, 19.8, 36.9, "2026-04-15");

        mockMvc.perform(get("/api/ai-analysis/daily-log/" + dailyLogId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dailyLogId").value(dailyLogId.toString()))
                .andExpect(jsonPath("$.userId").value(user.userId()))
                .andExpect(jsonPath("$.date").value("2026-04-14"))
                .andExpect(jsonPath("$.totalSteps").value(12000))
                .andExpect(jsonPath("$.totalMeals").value(2))
                .andExpect(jsonPath("$.totalWorkoutSessions").value(1))
                .andExpect(jsonPath("$.totalCaloriesConsumed").value(420.0))
                .andExpect(jsonPath("$.totalCaloriesBurned").value(180.0))
                .andExpect(jsonPath("$.calorieBalance").value(240.0))
                .andExpect(jsonPath("$.goalType").value("LOSE_WEIGHT"))
                .andExpect(jsonPath("$.targetWeight").value(75.0))
                .andExpect(jsonPath("$.targetCalories").value(2100.0))
                .andExpect(jsonPath("$.latestWeight").value(80.7))
                .andExpect(jsonPath("$.latestBodyFat").value(19.8))
                .andExpect(jsonPath("$.latestMuscleMass").value(36.9))
                .andExpect(jsonPath("$.meals.length()").value(2))
                .andExpect(jsonPath("$.meals[0].mealType").value("BREAKFAST"))
                .andExpect(jsonPath("$.meals[0].totalItems").value(1))
                .andExpect(jsonPath("$.meals[0].totalCalories").value(120.0))
                .andExpect(jsonPath("$.meals[1].mealType").value("LUNCH"))
                .andExpect(jsonPath("$.meals[1].totalItems").value(1))
                .andExpect(jsonPath("$.meals[1].totalCalories").value(300.0))
                .andExpect(jsonPath("$.workouts.length()").value(1))
                .andExpect(jsonPath("$.workouts[0].exerciseName").value("Press"))
                .andExpect(jsonPath("$.workouts[0].duration").value(30))
                .andExpect(jsonPath("$.workouts[0].caloriesBurned").value(180.0));
    }

    @Test
    void aiAnalysisShouldReturnZeroAndNullWhenOptionalDataIsMissing() throws Exception {
        UserContext user = registerAndLogin();
        String token = user.token();

        UUID dailyLogId = createDailyLog(token, user.userId());

        mockMvc.perform(get("/api/ai-analysis/daily-log/" + dailyLogId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dailyLogId").value(dailyLogId.toString()))
                .andExpect(jsonPath("$.totalMeals").value(0))
                .andExpect(jsonPath("$.totalWorkoutSessions").value(0))
                .andExpect(jsonPath("$.totalCaloriesConsumed").value(0.0))
                .andExpect(jsonPath("$.totalCaloriesBurned").value(0.0))
                .andExpect(jsonPath("$.calorieBalance").value(0.0))
                .andExpect(jsonPath("$.goalType").value(nullValue()))
                .andExpect(jsonPath("$.targetWeight").value(nullValue()))
                .andExpect(jsonPath("$.targetCalories").value(nullValue()))
                .andExpect(jsonPath("$.latestWeight").value(nullValue()))
                .andExpect(jsonPath("$.latestBodyFat").value(nullValue()))
                .andExpect(jsonPath("$.latestMuscleMass").value(nullValue()))
                .andExpect(jsonPath("$.meals.length()").value(0))
                .andExpect(jsonPath("$.workouts.length()").value(0));
    }

    @Test
    void aiAnalysisShouldReturnNotFoundForMissingDailyLog() throws Exception {
        UserContext user = registerAndLogin();
        UUID randomId = UUID.randomUUID();

        mockMvc.perform(get("/api/ai-analysis/daily-log/" + randomId)
                        .header("Authorization", "Bearer " + user.token()))
                .andExpect(status().isNotFound());
    }

    private UUID createDailyLog(String token, String userId) throws Exception {
        String dailyLogBody = """
                {
                  "logDate": "2026-04-14",
                  "steps": 12000,
                  "caloriesConsumed": 0.0,
                  "caloriesBurned": 0.0,
                  "userId": "%s"
                }
                """.formatted(userId);

        MvcResult result = mockMvc.perform(post("/api/daily-logs")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dailyLogBody))
                .andExpect(status().isCreated())
                .andReturn();

        return UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id")
                .asText());
    }

    private UUID createMeal(String token, UUID dailyLogId, String mealType) throws Exception {
        String mealBody = """
                {
                  "mealType": "%s",
                  "dailyLogId": "%s"
                }
                """.formatted(mealType, dailyLogId);

        MvcResult result = mockMvc.perform(post("/api/meals")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mealBody))
                .andExpect(status().isCreated())
                .andReturn();

        return UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id")
                .asText());
    }

    private UUID createFood(String token, String name, Double calories) throws Exception {
        String foodBody = """
                {
                  "name": "%s",
                  "calories": %s,
                  "protein": 10.0,
                  "carbs": 20.0,
                  "fat": 5.0
                }
                """.formatted(name, String.valueOf(calories));

        MvcResult result = mockMvc.perform(post("/api/foods")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(foodBody))
                .andExpect(status().isCreated())
                .andReturn();

        return UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id")
                .asText());
    }

    private void createMealItem(String token, UUID mealId, UUID foodId, Integer quantity) throws Exception {
        String mealItemBody = """
                {
                  "mealId": "%s",
                  "foodId": "%s",
                  "quantity": %d
                }
                """.formatted(mealId, foodId, quantity);

        mockMvc.perform(post("/api/meal-items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mealItemBody))
                .andExpect(status().isCreated())
                .andReturn();
    }

    private UUID createExercise(String token) throws Exception {
        String exerciseBody = """
                {
                  "name": "Press",
                  "muscleGroup": "Chest",
                  "equipment": "Barbell",
                  "description": "Bench press"
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/exercises")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(exerciseBody))
                .andExpect(status().isCreated())
                .andReturn();

        return UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id")
                .asText());
    }

    private void createWorkoutSession(String token, UUID dailyLogId, UUID exerciseId, Double caloriesBurned) throws Exception {
        String workoutSessionBody = """
                {
                  "dailyLogId": "%s",
                  "exerciseId": "%s",
                  "sets": 4,
                  "reps": 10,
                  "duration": 30,
                  "caloriesBurned": %s
                }
                """.formatted(dailyLogId, exerciseId, String.valueOf(caloriesBurned));

        mockMvc.perform(post("/api/workouts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(workoutSessionBody))
                .andExpect(status().isCreated())
                .andReturn();
    }

    private void createGoal(String token, String userId) throws Exception {
        String goalBody = """
                {
                  "userId": "%s",
                  "goalType": "LOSE_WEIGHT",
                  "targetWeight": 75,
                  "targetCalories": 2100
                }
                """.formatted(userId);

        mockMvc.perform(post("/api/goals")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(goalBody))
                .andExpect(status().isCreated())
                .andReturn();
    }

    private void createBodyMetric(String token, String userId, Double weight, Double bodyFat, Double muscleMass, String date) throws Exception {
        String bodyMetricBody = """
                {
                  "userId": "%s",
                  "weight": %s,
                  "bodyFat": %s,
                  "muscleMass": %s,
                  "date": "%s"
                }
                """.formatted(userId, String.valueOf(weight), String.valueOf(bodyFat), String.valueOf(muscleMass), date);

        mockMvc.perform(post("/api/body-metrics")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyMetricBody))
                .andExpect(status().isCreated())
                .andReturn();
    }

    private UserContext registerAndLogin() throws Exception {
        String email = "ai-" + UUID.randomUUID() + "@example.com";
        String password = "Passw0rd!";

        String registerBody = """
                {
                  "name": "AI User",
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
