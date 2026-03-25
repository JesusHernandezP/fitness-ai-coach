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
public class DailyLogCalorieBalanceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void swaggerSpecShouldExposeCalorieBalanceEndpoint() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['paths']['/api/daily-logs/{id}/calorie-balance']['get']").exists());
    }

    @Test
    void calorieBalanceShouldReturnConsumedMinusBurned() throws Exception {
        UserContext user = registerAndLogin();
        String token = user.token();

        UUID dailyLogId = createDailyLog(token, user.userId());
        UUID mealId = createMeal(token, dailyLogId);
        UUID foodId = createFood(token, "Chicken Breast", 200.0);
        createMealItem(token, mealId, foodId, 1);
        UUID exerciseId = createExercise(token);
        createWorkoutSession(token, dailyLogId, exerciseId, 75.0);

        mockMvc.perform(get("/api/daily-logs/" + dailyLogId + "/calorie-balance")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dailyLogId").value(dailyLogId.toString()))
                .andExpect(jsonPath("$.caloriesConsumed").value(200.0))
                .andExpect(jsonPath("$.caloriesBurned").value(75.0))
                .andExpect(jsonPath("$.calorieBalance").value(125.0));
    }

    @Test
    void calorieBalanceShouldReturnZeroValuesWhenNoMealsOrWorkouts() throws Exception {
        UserContext user = registerAndLogin();
        String token = user.token();
        UUID dailyLogId = createDailyLog(token, user.userId());

        mockMvc.perform(get("/api/daily-logs/" + dailyLogId + "/calorie-balance")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dailyLogId").value(dailyLogId.toString()))
                .andExpect(jsonPath("$.caloriesConsumed").value(0.0))
                .andExpect(jsonPath("$.caloriesBurned").value(0.0))
                .andExpect(jsonPath("$.calorieBalance").value(0.0));
    }

    @Test
    void calorieBalanceShouldReturnNotFoundForMissingDailyLog() throws Exception {
        UserContext user = registerAndLogin();
        UUID randomId = UUID.randomUUID();

        mockMvc.perform(get("/api/daily-logs/" + randomId + "/calorie-balance")
                        .header("Authorization", "Bearer " + user.token()))
                .andExpect(status().isNotFound());
    }

    private UUID createDailyLog(String token, String userId) throws Exception {
        String dailyLogBody = """
                {
                  "logDate": "2026-04-20",
                  "steps": 8000,
                  "caloriesConsumed": 2500.0,
                  "caloriesBurned": 1200.0,
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

    private UUID createMeal(String token, UUID dailyLogId) throws Exception {
        String mealBody = """
                {
                  "mealType": "LUNCH",
                  "dailyLogId": "%s"
                }
                """.formatted(dailyLogId);

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
                  "protein": 0.0,
                  "carbs": 0.0,
                  "fat": 0.0
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

    private UserContext registerAndLogin() throws Exception {
        String email = "balance-" + UUID.randomUUID() + "@example.com";
        String password = "Passw0rd!";

        String registerBody = """
                {
                  "name": "Balance User",
                  "email": "%s",
                  "password": "%s",
                  "age": 32,
                  "heightCm": 179.0,
                  "weightKg": 74.0
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
