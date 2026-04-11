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

import static org.hamcrest.Matchers.greaterThan;
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
                  "userId": "%s",
                  "goalType": "LOSE_WEIGHT",
                  "targetWeight": 75,
                  "targetCalories": 2000
                }
                """.formatted(user.userId());

        String createResult = mockMvc.perform(post("/api/goals")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(goalBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.goalType").value("LOSE_WEIGHT"))
                .andExpect(jsonPath("$.data.targetWeight").value(75.0))
                .andExpect(jsonPath("$.data.targetCalories", greaterThan(0.0)))
                .andExpect(jsonPath("$.data.targetProtein", greaterThan(0.0)))
                .andExpect(jsonPath("$.data.targetCarbs", greaterThan(0.0)))
                .andExpect(jsonPath("$.data.targetFat", greaterThan(0.0)))
                .andExpect(jsonPath("$.data.userId").value(user.userId()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String goalId = objectMapper.readTree(createResult).get("data").get("id").asText();

        mockMvc.perform(get("/api/goals")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isArray());

        mockMvc.perform(get("/api/goals/" + goalId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value(goalId))
                .andExpect(jsonPath("$.data.goalType").value("LOSE_WEIGHT"));

        mockMvc.perform(delete("/api/goals/" + goalId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
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
                .andExpect(jsonPath("$.data.token").isNotEmpty())
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
