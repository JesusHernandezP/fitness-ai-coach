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
public class MealControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void swaggerSpecShouldExposeMealEndpoints() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['paths']['/api/meals']").exists())
                .andExpect(jsonPath("$['paths']['/api/meals/{id}']").exists())
                .andExpect(jsonPath("$['paths']['/api/meals/{id}']['delete']").exists())
                .andExpect(jsonPath("$['paths']['/api/meals/{id}']['get']").exists());
    }

    @Test
    void mealCrudEndpointsShouldWorkWithValidToken() throws Exception {
        UserContext user = registerAndLogin();
        String token = user.token();
        UUID dailyLogId = createDailyLog(token, user.userId());

        String mealBody = """
                {
                  "mealType": "LUNCH",
                  "dailyLogId": "%s"
                }
                """.formatted(dailyLogId);

        MvcResult createMealResult = mockMvc.perform(post("/api/meals")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mealBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mealType").value("LUNCH"))
                .andExpect(jsonPath("$.dailyLogId").value(dailyLogId.toString()))
                .andReturn();

        String mealId = objectMapper.readTree(createMealResult.getResponse().getContentAsString())
                .get("id")
                .asText();

        mockMvc.perform(get("/api/meals")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/meals/" + mealId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(mealId))
                .andExpect(jsonPath("$.mealType").value("LUNCH"));

        mockMvc.perform(delete("/api/meals/" + mealId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/meals/" + mealId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    private UUID createDailyLog(String token, String userId) throws Exception {
        String dailyLogBody = """
                {
                  "logDate": "2026-04-15",
                  "steps": 5000,
                  "caloriesConsumed": 2200.0,
                  "caloriesBurned": 300.0,
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

    private UserContext registerAndLogin() throws Exception {
        String email = "meal-" + UUID.randomUUID() + "@example.com";
        String password = "Passw0rd!";

        String registerBody = """
                {
                  "name": "Meal User",
                  "email": "%s",
                  "password": "%s",
                  "age": 30,
                  "heightCm": 180.0,
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
