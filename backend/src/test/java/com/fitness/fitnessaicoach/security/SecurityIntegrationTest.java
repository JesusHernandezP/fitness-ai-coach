package com.fitness.fitnessaicoach.security;

import com.fasterxml.jackson.databind.JsonNode;
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
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getUsersWithoutTokenShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
<<<<<<< HEAD
=======
    void groqHealthWithoutTokenShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/health/groq"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void actuatorHealthWithoutTokenShouldReturn200() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
>>>>>>> main
    void getExercisesWithoutTokenShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/exercises"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createExerciseWithoutTokenShouldReturn401() throws Exception {
        String exerciseBody = """
                {
                  "name": "Push Up",
                  "muscleGroup": "Chest",
                  "equipment": "Bodyweight",
                  "description": "Basic push exercise"
                }
                """;

        mockMvc.perform(post("/api/exercises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(exerciseBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void exerciseEndpointsWithValidTokenShouldReturnSuccess() throws Exception {
        String token = registerAndLogin();

        String exerciseBody = """
                {
                  "name": "Push Up",
                  "muscleGroup": "Chest",
                  "equipment": "Bodyweight",
                  "description": "Basic push exercise"
                }
                """;

        mockMvc.perform(post("/api/exercises")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(exerciseBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Push Up"));

        mockMvc.perform(get("/api/exercises")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void getUsersWithValidTokenShouldReturn200() throws Exception {
        String token = registerAndLogin();

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    private String registerAndLogin() throws Exception {
        String email = "security-" + UUID.randomUUID() + "@example.com";
        String password = "Passw0rd!";

        String registerBody = """
                {
                  "name": "Security Test",
                  "email": "%s",
                  "password": "%s",
                  "age": 25,
                  "heightCm": 175.0,
                  "weightKg": 70.0
                }
                """.formatted(email, password);

<<<<<<< HEAD
        mockMvc.perform(post("/api/users")
=======
        mockMvc.perform(post("/api/auth/register")
>>>>>>> main
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
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        return jsonNode.get("token").asText();
    }
}
