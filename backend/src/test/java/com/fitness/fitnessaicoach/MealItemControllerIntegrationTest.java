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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
public class MealItemControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void swaggerSpecShouldExposeMealItemEndpoints() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['paths']['/api/meal-items']").exists())
                .andExpect(jsonPath("$['paths']['/api/meal-items']['post']").exists())
                .andExpect(jsonPath("$['paths']['/api/meal-items']['get']").exists())
                .andExpect(jsonPath("$['paths']['/api/meal-items/{id}']['delete']").exists());
    }

    @Test
    void mealItemCrudEndpointsShouldWorkWithValidToken() throws Exception {
        UserContext user = registerAndLogin();
        String token = user.token();
        UUID dailyLogId = createDailyLog(token, user.userId());
        UUID mealId = createMeal(token, dailyLogId);
        UUID foodId = createFood(token);

        String mealItemBody = """
                {
                  "mealId": "%s",
                  "foodId": "%s",
                  "quantity": 2
                }
                """.formatted(mealId, foodId);

        MvcResult createMealItemResult = mockMvc.perform(post("/api/meal-items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mealItemBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mealId").value(mealId.toString()))
                .andExpect(jsonPath("$.foodId").value(foodId.toString()))
                .andExpect(jsonPath("$.quantity").value(2.0))
                .andReturn();

        String mealItemId = objectMapper.readTree(createMealItemResult.getResponse().getContentAsString())
                .get("id")
                .asText();

        assertThat(objectMapper.readTree(createMealItemResult.getResponse().getContentAsString())
                .get("calculatedCalories").asDouble())
                .isEqualTo(300.0);

        mockMvc.perform(get("/api/meal-items")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/meal-items/" + mealItemId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    private UUID createDailyLog(String token, String userId) throws Exception {
        String dailyLogBody = """
                {
                  "logDate": "2026-04-15",
                  "steps": 6000,
                  "caloriesConsumed": 2500.0,
                  "caloriesBurned": 500.0,
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

    private UUID createFood(String token) throws Exception {
        String foodBody = """
                {
                  "name": "Chicken Breast",
                  "calories": 150.0,
                  "protein": 31.0,
                  "carbs": 0.0,
                  "fat": 3.6
                }
                """;

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

    private UserContext registerAndLogin() throws Exception {
        String email = "mealitem-" + UUID.randomUUID() + "@example.com";
        String password = "Passw0rd!";

        String registerBody = """
                {
                  "name": "MealItem User",
                  "email": "%s",
                  "password": "%s",
                  "age": 32,
                  "heightCm": 178.0,
                  "weightKg": 75.0
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
