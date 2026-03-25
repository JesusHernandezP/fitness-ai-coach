package com.fitness.fitnessaicoach;

import com.fitness.fitnessaicoach.ai.provider.groq.GroqClient;
import com.fitness.fitnessaicoach.controller.HealthController;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HealthControllerTest {

    @Test
    void groqHealthEndpointReturnsConnectionStatus() throws Exception {
        GroqClient groqClient = mock(GroqClient.class);
        when(groqClient.checkConnectivity())
                .thenReturn(new GroqClient.ConnectionStatus(false, 401, "Groq API key is not configured."));

        HealthController controller = new HealthController(groqClient);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/api/health/groq"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false))
                .andExpect(jsonPath("$.statusCode").value(401))
                .andExpect(jsonPath("$.message").value("Groq API key is not configured."));
    }
}

