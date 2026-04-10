package com.fitness.fitnessaicoach;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RequestValidationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createUserShouldReturn400ForInvalidEmailAndShortPassword() throws Exception {
        String invalidUserBody = """
                {
                  "name": "Validation User",
                  "email": "invalid-email",
                  "password": "123",
                  "age": 30,
                  "heightCm": 175.0,
                  "weightKg": 70.0
                }
                """;

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidUserBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void createFoodShouldReturn400ForNegativeMacros() throws Exception {
        String token = registerAndLogin();
        String invalidFoodBody = """
                {
                  "name": "Invalid Food",
                  "calories": -10.0,
                  "protein": -1.0,
                  "carbs": -2.0,
                  "fat": -3.0
                }
                """;

        mockMvc.perform(post("/api/foods")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidFoodBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.calories").exists())
                .andExpect(jsonPath("$.errors.protein").exists())
                .andExpect(jsonPath("$.errors.carbs").exists())
                .andExpect(jsonPath("$.errors.fat").exists());
    }

    @Test
    void createDailyLogShouldReturn400ForNegativeActivityValues() throws Exception {
        UserContext user = registerAndLoginWithContext();
        String invalidDailyLogBody = """
                {
                  "logDate": "2026-04-10",
                  "steps": -1,
                  "caloriesConsumed": -2000.0,
                  "caloriesBurned": -300.0,
                  "userId": "%s"
                }
                """.formatted(user.userId());

        mockMvc.perform(post("/api/daily-logs")
                        .header("Authorization", "Bearer " + user.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidDailyLogBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.steps").exists())
                .andExpect(jsonPath("$.errors.caloriesConsumed").exists())
                .andExpect(jsonPath("$.errors.caloriesBurned").exists());
    }

    private String registerAndLogin() throws Exception {
        return registerAndLoginWithContext().token();
    }

    private UserContext registerAndLoginWithContext() throws Exception {
        String email = "validation-" + UUID.randomUUID() + "@example.com";
        String password = "Passw0rd!";

        String registerBody = """
                {
                  "name": "Validation Test User",
                  "email": "%s",
                  "password": "%s",
                  "age": 27,
                  "heightCm": 172.0,
                  "weightKg": 68.0
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
