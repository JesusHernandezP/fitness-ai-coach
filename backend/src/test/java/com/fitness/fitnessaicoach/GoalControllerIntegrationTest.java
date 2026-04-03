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
public class GoalControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void swaggerSpecShouldExposeGoalEndpoints() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['paths']['/api/goals']").exists())
                .andExpect(jsonPath("$['paths']['/api/goals']['post']").exists())
                .andExpect(jsonPath("$['paths']['/api/goals']['get']").exists())
                .andExpect(jsonPath("$['paths']['/api/goals/{id}']['get']").exists())
                .andExpect(jsonPath("$['paths']['/api/goals/{id}']['delete']").exists());
    }

    @Test
    void goalCrudEndpointsShouldWorkWithValidToken() throws Exception {
        UserContext user = registerAndLogin();
        String token = user.token();

        String goalBody = """
                {
                  "goalType": "LOSE_WEIGHT",
                  "targetWeight": 75,
                  "targetCalories": 2000
                }
                """;

        String createResult = mockMvc.perform(post("/api/goals")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(goalBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.goalType").value("LOSE_WEIGHT"))
                .andExpect(jsonPath("$.targetWeight").value(75.0))
                .andExpect(jsonPath("$.targetCalories").value(2000))
                .andExpect(jsonPath("$.userId").value(user.userId()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String goalId = objectMapper.readTree(createResult).get("id").asText();

        mockMvc.perform(get("/api/goals")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(goalId));

        mockMvc.perform(get("/api/goals/" + goalId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(goalId))
                .andExpect(jsonPath("$.goalType").value("LOSE_WEIGHT"));

        mockMvc.perform(delete("/api/goals/" + goalId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void goalShouldAllowMissingTargetWeight() throws Exception {
        UserContext user = registerAndLogin();

        String goalBody = """
                {
                  "goalType": "MAINTAIN",
                  "targetCalories": 2200
                }
                """;

        mockMvc.perform(post("/api/goals")
                        .header("Authorization", "Bearer " + user.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(goalBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.goalType").value("MAINTAIN"))
                .andExpect(jsonPath("$.targetWeight").isEmpty())
                .andExpect(jsonPath("$.targetCalories").value(2200));
    }

    private UserContext registerAndLogin() throws Exception {
        String email = "goal-" + UUID.randomUUID() + "@example.com";
        String password = "Passw0rd!";

        String registerBody = """
                {
                  "name": "Goal User",
                  "email": "%s",
                  "password": "%s",
                  "age": 31,
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
