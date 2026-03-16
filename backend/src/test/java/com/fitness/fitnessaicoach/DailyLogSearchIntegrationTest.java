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

import java.time.LocalDate;
import java.util.UUID;

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
public class DailyLogSearchIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void swaggerSpecShouldExposeDailyLogSearchEndpoints() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['paths']['/api/daily-logs/user/{userId}']['get']").exists())
                .andExpect(jsonPath("$['paths']['/api/daily-logs/user/{userId}/date/{date}']['get']").exists());
    }

    @Test
    void shouldReturnLogsByUser() throws Exception {
        UserContext user = registerAndLogin();
        String token = user.token();

        createDailyLog(token, user.userId(), LocalDate.parse("2026-04-14"), 8000);
        createDailyLog(token, user.userId(), LocalDate.parse("2026-04-15"), 10500);

        mockMvc.perform(get("/api/daily-logs/user/" + user.userId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].userId").value(user.userId()))
                .andExpect(jsonPath("$[1].userId").value(user.userId()));
    }

    @Test
    void shouldReturnLogByUserAndDate() throws Exception {
        UserContext user = registerAndLogin();
        String token = user.token();

        createDailyLog(token, user.userId(), LocalDate.parse("2026-04-14"), 8000);
        createDailyLog(token, user.userId(), LocalDate.parse("2026-04-15"), 10500);

        mockMvc.perform(get("/api/daily-logs/user/" + user.userId() + "/date/2026-04-15")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2026-04-15"))
                .andExpect(jsonPath("$.steps").value(10500))
                .andExpect(jsonPath("$.userId").value(user.userId()));
    }

    @Test
    void shouldReturnNotFoundWhenDateLogDoesNotExist() throws Exception {
        UserContext user = registerAndLogin();
        String token = user.token();

        createDailyLog(token, user.userId(), LocalDate.parse("2026-04-14"), 8000);

        mockMvc.perform(get("/api/daily-logs/user/" + user.userId() + "/date/2026-04-15")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    private void createDailyLog(String token, String userId, LocalDate date, Integer steps) throws Exception {
        String dailyLogBody = """
                {
                  "logDate": "%s",
                  "steps": %d,
                  "caloriesConsumed": 2000.0,
                  "caloriesBurned": 300.0,
                  "userId": "%s"
                }
                """.formatted(date, steps, userId);

        mockMvc.perform(post("/api/daily-logs")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dailyLogBody))
                .andExpect(status().isCreated())
                .andReturn();
    }

    private UserContext registerAndLogin() throws Exception {
        String email = "search-" + UUID.randomUUID() + "@example.com";
        String password = "Passw0rd!";

        String registerBody = """
                {
                  "name": "Daily Log Search User",
                  "email": "%s",
                  "password": "%s",
                  "age": 29,
                  "heightCm": 176.0,
                  "weightKg": 70.0
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
