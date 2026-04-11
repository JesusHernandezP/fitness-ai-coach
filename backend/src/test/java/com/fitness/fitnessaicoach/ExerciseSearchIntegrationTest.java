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
public class ExerciseSearchIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void swaggerSpecShouldExposeExerciseSearchEndpoint() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['paths']['/api/exercises']['get']").exists())
                .andExpect(jsonPath("$['paths']['/api/exercises/search']['get']").exists());
    }

    @Test
    void shouldReturnExerciseCatalog() throws Exception {
        String token = registerAndLogin();
        String uniqueTerm = "Press-" + UUID.randomUUID();

        createExercise(token, "Bench " + uniqueTerm, "Chest", "Barbell", "Chest strength exercise");
        createExercise(token, "Squat " + uniqueTerm, "Legs", "Barbell", "Leg compound exercise");

        mockMvc.perform(get("/api/exercises")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name=='Bench " + uniqueTerm + "')]").exists())
                .andExpect(jsonPath("$[?(@.name=='Squat " + uniqueTerm + "')]").exists());
    }

    @Test
    void shouldSearchExercisesByPartialNameIgnoringCase() throws Exception {
        String token = registerAndLogin();
        String uniqueTerm = "Bench-" + UUID.randomUUID();

        createExercise(token, uniqueTerm + " press", "Chest", "Barbell", "Chest strength exercise");
        createExercise(token, "Incline " + uniqueTerm + " press", "Chest", "Barbell", "Upper chest exercise");
        createExercise(token, "Running " + uniqueTerm, "Cardio", "None", "Cardio exercise");

        mockMvc.perform(get("/api/exercises/search")
                        .param("query", uniqueTerm.toUpperCase())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].name").value(uniqueTerm + " press"))
                .andExpect(jsonPath("$[1].name").value("Incline " + uniqueTerm + " press"))
                .andExpect(jsonPath("$[2].name").value("Running " + uniqueTerm));
    }

    @Test
    void shouldReturnEmptyListWhenSearchQueryIsBlank() throws Exception {
        String token = registerAndLogin();

        mockMvc.perform(get("/api/exercises/search")
                        .param("query", "   ")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    private void createExercise(String token, String name, String muscleGroup, String equipment, String description) throws Exception {
        String exerciseBody = """
                {
                  "name": "%s",
                  "muscleGroup": "%s",
                  "equipment": "%s",
                  "description": "%s"
                }
                """.formatted(name, muscleGroup, equipment, description);

        mockMvc.perform(post("/api/exercises")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(exerciseBody))
                .andExpect(status().isCreated());
    }

    private String registerAndLogin() throws Exception {
        String email = "exercise-search-" + UUID.randomUUID() + "@example.com";
        String password = "Passw0rd!";

        String registerBody = """
                {
                  "name": "Exercise Search User",
                  "email": "%s",
                  "password": "%s",
                  "age": 29,
                  "heightCm": 176.0,
                  "weightKg": 73.0
                }
                """.formatted(email, password);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isCreated());

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

        return objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("data")
                .get("token")
                .asText();
    }
}
