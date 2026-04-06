package com.fitness.fitnessaicoach;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.fitnessaicoach.ai.provider.AITextGenerationClient;
import com.fitness.fitnessaicoach.dto.ai.AIFoodEstimateResponse;
import com.fitness.fitnessaicoach.service.AIFoodEstimationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AIFoodEstimationServiceTest {

    @Mock
    private AITextGenerationClient aiTextGenerationClient;

    @Test
    void shouldFallbackWhenAiResponseIsNotValidJson() {
        AIFoodEstimationService service = new AIFoodEstimationService(aiTextGenerationClient, new ObjectMapper());
        when(aiTextGenerationClient.generateText(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn("Chicken with rice is a balanced meal with protein and carbs.");

        AIFoodEstimateResponse estimate = service.estimateFood("chicken");

        assertThat(estimate.getName()).isEqualTo("chicken");
        assertThat(estimate.getCalories()).isEqualTo(165.0);
        assertThat(estimate.getProtein()).isEqualTo(31.0);
        assertThat(estimate.getCarbs()).isEqualTo(0.0);
        assertThat(estimate.getFat()).isEqualTo(3.6);
    }
}
