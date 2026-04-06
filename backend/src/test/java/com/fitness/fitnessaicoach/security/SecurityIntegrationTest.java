package com.fitness.fitnessaicoach.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.fitnessaicoach.domain.User;
import com.fitness.fitnessaicoach.repository.UserRepository;
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

    @Autowired
    private UserRepository userRepository;

    @Test
    void getUsersWithoutTokenShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
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

    @Test
    void chatHistoryWithoutTokenShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/ai-chat/history"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void weeklySummaryWithoutTokenShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/ai-coach/weekly-summary"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registeredPasswordShouldBeStoredEncrypted() throws Exception {
        String email = "security-password-" + UUID.randomUUID() + "@example.com";
        String rawPassword = "Passw0rd!";

        String registerBody = """
                {
                  "name": "Password Test",
                  "email": "%s",
                  "password": "%s",
                  "age": 25,
                  "heightCm": 175.0,
                  "weightKg": 70.0
                }
                """.formatted(email, rawPassword);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isOk());

        User savedUser = userRepository.findByEmail(email).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(savedUser.getPassword()).isNotEqualTo(rawPassword);
        org.assertj.core.api.Assertions.assertThat(savedUser.getPassword()).startsWith("$2");
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

        mockMvc.perform(post("/api/users")
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
