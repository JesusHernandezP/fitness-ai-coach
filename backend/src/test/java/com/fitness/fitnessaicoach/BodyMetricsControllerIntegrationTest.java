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
public class BodyMetricsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void swaggerSpecShouldExposeBodyMetricsEndpoints() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['paths']['/api/body-metrics']").exists())
                .andExpect(jsonPath("$['paths']['/api/body-metrics']['post']").exists())
                .andExpect(jsonPath("$['paths']['/api/body-metrics']['get']").exists())
                .andExpect(jsonPath("$['paths']['/api/body-metrics/{id}']['get']").exists())
                .andExpect(jsonPath("$['paths']['/api/body-metrics/{id}']['delete']").exists());
    }

    @Test
    void bodyMetricsCrudEndpointsShouldUseAuthenticatedUser() throws Exception {
        UserContext user = registerAndLogin("bodymetrics");
        UserContext otherUser = registerAndLogin("other");

        String bodyMetricsBody = """
                {
                  "weight": 82.5,
                  "date": "2026-04-15"
                }
                """;

        String createResult = mockMvc.perform(post("/api/body-metrics")
                        .header("Authorization", "Bearer " + user.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyMetricsBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.weight").value(82.5))
                .andExpect(jsonPath("$.date").value("2026-04-15"))
                .andExpect(jsonPath("$.userId").value(user.userId()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String bodyMetricsId = objectMapper.readTree(createResult).get("id").asText();

        mockMvc.perform(get("/api/body-metrics")
                        .header("Authorization", "Bearer " + user.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(bodyMetricsId))
                .andExpect(jsonPath("$[0].userId").value(user.userId()));

        mockMvc.perform(get("/api/body-metrics")
                        .header("Authorization", "Bearer " + otherUser.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/api/body-metrics/" + bodyMetricsId)
                        .header("Authorization", "Bearer " + user.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bodyMetricsId))
                .andExpect(jsonPath("$.userId").value(user.userId()));

        mockMvc.perform(get("/api/body-metrics/" + bodyMetricsId)
                        .header("Authorization", "Bearer " + otherUser.token()))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/body-metrics/" + bodyMetricsId)
                        .header("Authorization", "Bearer " + otherUser.token()))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/body-metrics/" + bodyMetricsId)
                        .header("Authorization", "Bearer " + user.token()))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldRejectDuplicateBodyMetricsForSameUserAndDate() throws Exception {
        UserContext user = registerAndLogin("duplicate");

        String bodyMetricsBody = """
                {
                  "weight": 82.5,
                  "date": "2026-04-15"
                }
                """;

        mockMvc.perform(post("/api/body-metrics")
                        .header("Authorization", "Bearer " + user.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyMetricsBody))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/body-metrics")
                        .header("Authorization", "Bearer " + user.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyMetricsBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("You already recorded your weight today"));
    }

    private UserContext registerAndLogin(String prefix) throws Exception {
        String email = prefix + "-" + UUID.randomUUID() + "@example.com";
        String password = "Passw0rd!";

        String registerBody = """
                {
                  "name": "Body Metrics User",
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
