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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ErrorHandlingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ──────────────────────────────────────────────────────────────────────
    //  401 UNAUTHORIZED
    // ──────────────────────────────────────────────────────────────────────

    @Test
    void accessingProtectedEndpointWithoutTokenShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/body-metrics"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void accessingProtectedEndpointWithInvalidTokenShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/body-metrics")
                        .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized());
    }

    // ──────────────────────────────────────────────────────────────────────
    //  404 NOT FOUND
    // ──────────────────────────────────────────────────────────────────────

    @Test
    void getNonExistingBodyMetricsShouldReturn404() throws Exception {
        String token = registerAndLogin();
        UUID fakeId = UUID.randomUUID();

        mockMvc.perform(get("/api/body-metrics/" + fakeId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Body metrics not found."));
    }

    @Test
    void getNonExistingGoalShouldReturn404() throws Exception {
        String token = registerAndLogin();
        UUID fakeId = UUID.randomUUID();

        mockMvc.perform(get("/api/goals/" + fakeId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Goal not found."));
    }

    // ──────────────────────────────────────────────────────────────────────
    //  400 BAD REQUEST (Validation)
    // ──────────────────────────────────────────────────────────────────────

    @Test
    void createGoalWithInvalidPayloadShouldReturn400AndErrorsMap() throws Exception {
        String token = registerAndLogin();
        
        // Missing goalType and invalid negative calories/weight
        String invalidGoalBody = """
                {
                  "targetWeight": -10,
                  "targetCalories": 0
                }
                """;

        mockMvc.perform(post("/api/goals")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidGoalBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.goalType").exists())
                .andExpect(jsonPath("$.errors.targetWeight").exists())
                .andExpect(jsonPath("$.errors.targetCalories").exists());
    }

    // ──────────────────────────────────────────────────────────────────────
    //  Helper methods
    // ──────────────────────────────────────────────────────────────────────

    private String registerAndLogin() throws Exception {
        String email = "err-test-" + UUID.randomUUID() + "@example.com";
        String password = "Passw0rd!";

        String registerBody = """
                {
                  "name": "Error Handling User",
                  "email": "%s",
                  "password": "%s",
                  "age": 30,
                  "heightCm": 180.0,
                  "weightKg": 80.0
                }
                """.formatted(email, password);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isCreated());

        String loginBody = """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(email, password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("data")
                .get("token")
                .asText();
    }
}
