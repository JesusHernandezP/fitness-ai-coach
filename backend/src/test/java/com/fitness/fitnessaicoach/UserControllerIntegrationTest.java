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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldUpdateMetabolicProfileForAuthenticatedUser() throws Exception {
        UserContext user = registerAndLogin();

        String updateBody = """
                {
                  "age": 29,
                  "heightCm": 178.0,
                  "sex": "FEMALE",
                  "activityLevel": "ACTIVE"
                }
                """;

        mockMvc.perform(put("/api/users/" + user.userId())
                        .header("Authorization", "Bearer " + user.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(user.userId()))
                .andExpect(jsonPath("$.data.age").value(29))
                .andExpect(jsonPath("$.data.heightCm").value(178.0))
                .andExpect(jsonPath("$.data.sex").value("FEMALE"))
                .andExpect(jsonPath("$.data.activityLevel").value("ACTIVE"));
    }

    @Test
    void shouldRejectOutOfRangeMetabolicProfileValues() throws Exception {
        UserContext user = registerAndLogin();

        String updateBody = """
                {
                  "age": 10,
                  "heightCm": 119.0,
                  "sex": "MALE",
                  "activityLevel": "MODERATE"
                }
                """;

        mockMvc.perform(put("/api/users/" + user.userId())
                        .header("Authorization", "Bearer " + user.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.age").value("Age must be at least 15."))
                .andExpect(jsonPath("$.errors.heightCm").value("Height must be at least 120 cm."));
    }

    @Test
    void shouldRecalculateGoalTargetsFromUpdatedMetabolicProfile() throws Exception {
        UserContext user = registerAndLogin();

        String updateBody = """
                {
                  "age": 29,
                  "heightCm": 178.0,
                  "sex": "FEMALE",
                  "activityLevel": "SEDENTARY"
                }
                """;

        mockMvc.perform(put("/api/users/" + user.userId())
                        .header("Authorization", "Bearer " + user.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk());

        String goalBody = """
                {
                  "goalType": "LOSE_WEIGHT",
                  "targetWeight": 68
                }
                """;

        mockMvc.perform(post("/api/goals")
                        .header("Authorization", "Bearer " + user.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(goalBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.targetCalories").value(1555.8))
                .andExpect(jsonPath("$.data.targetProtein").value(148.0))
                .andExpect(jsonPath("$.data.targetFat").value(59.2))
                .andExpect(jsonPath("$.data.targetCarbs").value(107.75));
    }

    private UserContext registerAndLogin() throws Exception {
        String email = "user-" + UUID.randomUUID() + "@example.com";
        String password = "Passw0rd!";

        String registerBody = """
                {
                  "name": "Profile User",
                  "email": "%s",
                  "password": "%s",
                  "age": 28,
                  "heightCm": 176.0,
                  "weightKg": 74.0
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
