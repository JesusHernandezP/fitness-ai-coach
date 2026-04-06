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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MetabolicProfileIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateProfileForAuthenticatedUser() throws Exception {
        UserContext user = registerAndLogin();

        mockMvc.perform(post("/api/users/profile")
                        .header("Authorization", "Bearer " + user.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validProfileBody(29, 178.0, "FEMALE", "ACTIVE")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(user.userId()))
                .andExpect(jsonPath("$.age").value(29))
                .andExpect(jsonPath("$.heightCm").value(178.0))
                .andExpect(jsonPath("$.sex").value("FEMALE"))
                .andExpect(jsonPath("$.activityLevel").value("ACTIVE"));
    }

    @Test
    void shouldGetCurrentProfile() throws Exception {
        UserContext user = registerAndLogin();

        mockMvc.perform(put("/api/users/profile")
                        .header("Authorization", "Bearer " + user.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validProfileBody(31, 181.5, "MALE", "MODERATE")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/users/profile")
                        .header("Authorization", "Bearer " + user.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(user.userId()))
                .andExpect(jsonPath("$.age").value(31))
                .andExpect(jsonPath("$.heightCm").value(181.5))
                .andExpect(jsonPath("$.sex").value("MALE"))
                .andExpect(jsonPath("$.activityLevel").value("MODERATE"));
    }

    @Test
    void shouldUpdateProfile() throws Exception {
        UserContext user = registerAndLogin();

        mockMvc.perform(post("/api/users/profile")
                        .header("Authorization", "Bearer " + user.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validProfileBody(28, 175.0, "FEMALE", "LIGHT")))
                .andExpect(status().isCreated());

        mockMvc.perform(put("/api/users/profile")
                        .header("Authorization", "Bearer " + user.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validProfileBody(30, 176.0, "FEMALE", "ACTIVE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.age").value(30))
                .andExpect(jsonPath("$.heightCm").value(176.0))
                .andExpect(jsonPath("$.activityLevel").value("ACTIVE"));
    }

    @Test
    void shouldRejectNegativeValues() throws Exception {
        UserContext user = registerAndLogin();

        mockMvc.perform(post("/api/users/profile")
                        .header("Authorization", "Bearer " + user.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validProfileBody(-1, -180.0, "MALE", "SEDENTARY")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.age").value("Age must be greater than 0."))
                .andExpect(jsonPath("$.errors.heightCm").value("Height must be greater than 0."));
    }

    private String validProfileBody(int age, double heightCm, String sex, String activityLevel) {
        return """
                {
                  "age": %d,
                  "heightCm": %s,
                  "sex": "%s",
                  "activityLevel": "%s"
                }
                """.formatted(age, heightCm, sex, activityLevel);
    }

    private UserContext registerAndLogin() throws Exception {
        String email = "profile-" + UUID.randomUUID() + "@example.com";
        String password = "Passw0rd!";

        String registerBody = """
                {
                  "name": "Profile User",
                  "email": "%s",
                  "password": "%s",
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
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("token")
                .asText();

        return new UserContext(token, userId);
    }

    private record UserContext(String token, String userId) {
    }
}
