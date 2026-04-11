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
    void bodyMetricsCrudEndpointsShouldWorkWithValidToken() throws Exception {
        UserContext user = registerAndLogin();
        String token = user.token();

        String body = """
                {
                  "userId": "%s",
                  "weight": 82.5,
                  "bodyFat": 18.2,
                  "muscleMass": 38.5,
                  "date": "2026-04-15"
                }
                """.formatted(user.userId());

        String response = mockMvc.perform(post("/api/body-metrics")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.userId").value(user.userId()))
                .andExpect(jsonPath("$.data.weight").value(82.5))
                .andExpect(jsonPath("$.data.bodyFat").value(18.2))
                .andExpect(jsonPath("$.data.muscleMass").value(38.5))
                .andExpect(jsonPath("$.data.date").value("2026-04-15"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID bodyMetricsId = UUID.fromString(objectMapper.readTree(response).get("data").get("id").asText());

        mockMvc.perform(get("/api/body-metrics")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isArray());

        mockMvc.perform(get("/api/body-metrics/" + bodyMetricsId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value(bodyMetricsId.toString()))
                .andExpect(jsonPath("$.data.weight").value(82.5));

        mockMvc.perform(delete("/api/body-metrics/" + bodyMetricsId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void bodyMetricsRequestShouldRejectInvalidBodyFatAndMuscleMass() throws Exception {
        UserContext user = registerAndLogin();
        String token = user.token();

        String invalidBodyFat = """
                {
                  "userId": "%s",
                  "weight": 82.5,
                  "bodyFat": 150,
                  "muscleMass": -10,
                  "date": "2026-04-15"
                }
                """.formatted(user.userId());

        mockMvc.perform(post("/api/body-metrics")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBodyFat))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.bodyFat").exists())
                .andExpect(jsonPath("$.errors.muscleMass").exists());

        String invalidWeight = """
                {
                  "userId": "%s",
                  "weight": -1,
                  "bodyFat": 20,
                  "muscleMass": 10,
                  "date": "2026-04-15"
                }
                """.formatted(user.userId());

        mockMvc.perform(post("/api/body-metrics")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidWeight))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.weight").exists());
    }

    private UserContext registerAndLogin() throws Exception {
        String email = "bodymetrics-" + UUID.randomUUID() + "@example.com";
        String password = "Passw0rd!";

        String registerBody = """
                {
                  "name": "Body Metrics User",
                  "email": "%s",
                  "password": "%s",
                  "age": 35,
                  "heightCm": 178.0,
                  "weightKg": 82.5
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
