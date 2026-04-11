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
public class FoodSearchIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void swaggerSpecShouldExposeFoodSearchEndpoint() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['paths']['/api/foods/search']['get']").exists());
    }

    @Test
    void shouldSearchFoodsByPartialNameIgnoringCase() throws Exception {
        UserContext user = registerAndLogin();
        String token = user.token();
        String uniqueTerm = "Chicken-" + UUID.randomUUID();

        createFood(token, uniqueTerm + " breast", 165.0, 31.0, 0.0, 3.6);
        createFood(token, "Grilled " + uniqueTerm, 180.0, 29.0, 1.0, 5.0);
        createFood(token, uniqueTerm + " rice", 220.0, 18.0, 24.0, 6.0);
        createFood(token, "Greek yogurt", 95.0, 10.0, 4.0, 0.0);

        mockMvc.perform(get("/api/foods/search")
                        .param("query", uniqueTerm.toUpperCase())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].name").value(uniqueTerm + " breast"))
                .andExpect(jsonPath("$[1].name").value(uniqueTerm + " rice"))
                .andExpect(jsonPath("$[2].name").value("Grilled " + uniqueTerm));
    }

    @Test
    void shouldReturnEmptyListWhenQueryIsBlank() throws Exception {
        UserContext user = registerAndLogin();

        mockMvc.perform(get("/api/foods/search")
                        .param("query", "   ")
                        .header("Authorization", "Bearer " + user.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldReturnEmptyListWhenQueryDoesNotMatchAnything() throws Exception {
        UserContext user = registerAndLogin();
        String token = user.token();

        createFood(token, "Chicken breast", 165.0, 31.0, 0.0, 3.6);

        mockMvc.perform(get("/api/foods/search")
                        .param("query", "salmon")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    private void createFood(String token, String name, double calories, double protein, double carbs, double fat) throws Exception {
        String foodBody = """
                {
                  "name": "%s",
                  "calories": %s,
                  "protein": %s,
                  "carbs": %s,
                  "fat": %s
                }
                """.formatted(name, calories, protein, carbs, fat);

        mockMvc.perform(post("/api/foods")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(foodBody))
                .andExpect(status().isCreated());
    }

    private UserContext registerAndLogin() throws Exception {
        String email = "food-search-" + UUID.randomUUID() + "@example.com";
        String password = "Passw0rd!";

        String registerBody = """
                {
                  "name": "Food Search User",
                  "email": "%s",
                  "password": "%s",
                  "age": 28,
                  "heightCm": 174.0,
                  "weightKg": 68.0
                }
                """.formatted(email, password);

        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
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
