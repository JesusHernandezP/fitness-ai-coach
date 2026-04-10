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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * T01 — Integration tests covering core endpoints that were not
 * explicitly validated by existing test classes.
 *
 * <p>Starts the full Spring context with H2 test profile, uses real
 * repositories and validates both HTTP status codes and JSON response
 * structure.
 *
 * <p>Endpoints covered here:
 * <ul>
 *   <li>POST /api/users  (register)</li>
 *   <li>POST /api/auth/login</li>
 *   <li>POST /api/foods  +  GET /api/foods</li>
 *   <li>POST /api/daily-logs  +  GET /api/daily-logs</li>
 * </ul>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CoreEndpointsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ──────────────────────────────────────────────────────────────────────
    //  POST /api/users  (register)
    // ──────────────────────────────────────────────────────────────────────

    @Test
    void registerShouldReturnUserResponseWithAllFields() throws Exception {
        String email = "core-reg-" + UUID.randomUUID() + "@example.com";

        String body = """
                {
                  "name": "Integration User",
                  "email": "%s",
                  "password": "Str0ngPwd!",
                  "age": 27,
                  "heightCm": 175.0,
                  "weightKg": 72.0
                }
                """.formatted(email);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Integration User"))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.age").value(27))
                .andExpect(jsonPath("$.heightCm").value(175.0))
                .andExpect(jsonPath("$.weightKg").value(72.0))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    void registerWithDuplicateEmailShouldReturn409() throws Exception {
        String email = "core-dup-" + UUID.randomUUID() + "@example.com";

        String body = """
                {
                  "name": "First User",
                  "email": "%s",
                  "password": "Str0ngPwd!",
                  "age": 30,
                  "heightCm": 180.0,
                  "weightKg": 80.0
                }
                """.formatted(email);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void registerWithMissingFieldsShouldReturn400() throws Exception {
        String body = """
                {
                  "email": "",
                  "password": ""
                }
                """;

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ──────────────────────────────────────────────────────────────────────
    //  POST /api/auth/login
    // ──────────────────────────────────────────────────────────────────────

    @Test
    void loginShouldReturnTokenOnValidCredentials() throws Exception {
        String email = "core-login-" + UUID.randomUUID() + "@example.com";
        String password = "Passw0rd!";

        registerUser(email, password);

        String loginBody = """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(email, password);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void loginWithWrongPasswordShouldReturn401() throws Exception {
        String email = "core-wrong-" + UUID.randomUUID() + "@example.com";

        registerUser(email, "CorrectPwd1!");

        String loginBody = """
                {
                  "email": "%s",
                  "password": "WrongPassword!"
                }
                """.formatted(email);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginWithNonExistentEmailShouldReturn401() throws Exception {
        String loginBody = """
                {
                  "email": "ghost-%s@example.com",
                  "password": "AnyPassword1!"
                }
                """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isUnauthorized());
    }

    // ──────────────────────────────────────────────────────────────────────
    //  POST /api/foods  +  GET /api/foods
    // ──────────────────────────────────────────────────────────────────────

    @Test
    void createFoodShouldReturnCreatedWithFullStructure() throws Exception {
        String token = registerAndLogin();

        String foodBody = """
                {
                  "name": "Brown Rice",
                  "calories": 216.0,
                  "protein": 5.0,
                  "carbs": 45.0,
                  "fat": 1.8
                }
                """;

        mockMvc.perform(post("/api/foods")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(foodBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Brown Rice"))
                .andExpect(jsonPath("$.calories").value(216.0))
                .andExpect(jsonPath("$.protein").value(5.0))
                .andExpect(jsonPath("$.carbs").value(45.0))
                .andExpect(jsonPath("$.fat").value(1.8));
    }

    @Test
    void getAllFoodsShouldReturnArrayIncludingCreatedFood() throws Exception {
        String token = registerAndLogin();
        String unique = "CoreFood-" + UUID.randomUUID();

        String foodBody = """
                {
                  "name": "%s",
                  "calories": 100.0,
                  "protein": 10.0,
                  "carbs": 12.0,
                  "fat": 2.0
                }
                """.formatted(unique);

        mockMvc.perform(post("/api/foods")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(foodBody))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/foods")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.name == '%s')]", unique).exists());
    }

    // ──────────────────────────────────────────────────────────────────────
    //  POST /api/daily-logs  +  GET /api/daily-logs
    // ──────────────────────────────────────────────────────────────────────

    @Test
    void createDailyLogShouldReturnCreatedWithFullStructure() throws Exception {
        UserContext user = registerAndLoginFull();
        String token = user.token();

        String body = """
                {
                  "logDate": "2026-04-10",
                  "steps": 9500,
                  "caloriesConsumed": 2200.0,
                  "caloriesBurned": 550.0,
                  "userId": "%s"
                }
                """.formatted(user.userId());

        mockMvc.perform(post("/api/daily-logs")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.date").value("2026-04-10"))
                .andExpect(jsonPath("$.steps").value(9500))
                .andExpect(jsonPath("$.caloriesConsumed").value(2200.0))
                .andExpect(jsonPath("$.caloriesBurned").value(550.0))
                .andExpect(jsonPath("$.userId").value(user.userId()));
    }

    @Test
    void getAllDailyLogsShouldReturnArray() throws Exception {
        UserContext user = registerAndLoginFull();
        String token = user.token();

        String body = """
                {
                  "logDate": "2026-04-09",
                  "steps": 6000,
                  "caloriesConsumed": 1800.0,
                  "caloriesBurned": 400.0,
                  "userId": "%s"
                }
                """.formatted(user.userId());

        mockMvc.perform(post("/api/daily-logs")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/daily-logs")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(1)));
    }

    // ──────────────────────────────────────────────────────────────────────
    //  Helper methods
    // ──────────────────────────────────────────────────────────────────────

    private void registerUser(String email, String password) throws Exception {
        String registerBody = """
                {
                  "name": "Core Test User",
                  "email": "%s",
                  "password": "%s",
                  "age": 28,
                  "heightCm": 176.0,
                  "weightKg": 73.0
                }
                """.formatted(email, password);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isOk());
    }

    private String registerAndLogin() throws Exception {
        String email = "core-" + UUID.randomUUID() + "@example.com";
        String password = "Passw0rd!";

        registerUser(email, password);

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
                .get("token")
                .asText();
    }

    private UserContext registerAndLoginFull() throws Exception {
        String email = "core-full-" + UUID.randomUUID() + "@example.com";
        String password = "Passw0rd!";

        String registerBody = """
                {
                  "name": "Core Full User",
                  "email": "%s",
                  "password": "%s",
                  "age": 30,
                  "heightCm": 180.0,
                  "weightKg": 78.0
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
