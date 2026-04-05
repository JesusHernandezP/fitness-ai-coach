package com.fitness.fitnessaicoach.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.fitnessaicoach.ai.provider.AITextGenerationClient;
import com.fitness.fitnessaicoach.dto.ai.AIFoodEstimateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AIFoodEstimationService {

    private final AITextGenerationClient aiTextGenerationClient;
    private final ObjectMapper objectMapper;

    public AIFoodEstimateResponse estimateFood(String foodName) {
        String prompt = """
                Estimate nutritional values for a single serving of the following food.
                Return valid JSON only with this exact shape:
                {"name":"string","calories":0,"protein":0,"carbs":0,"fat":0}

                Food: %s
                """.formatted(foodName);

        String response = sanitizeJson(aiTextGenerationClient.generateText(prompt));
        try {
            AIFoodEstimateResponse estimate = objectMapper.readValue(response, AIFoodEstimateResponse.class);
            if (estimate.getName() == null || estimate.getName().isBlank()) {
                estimate.setName(foodName);
            }
            return estimate;
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to parse AI food estimation response.", exception);
        }
    }

    private String sanitizeJson(String response) {
        if (response == null) {
            return "";
        }

        String trimmed = response.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceFirst("^```(?:json)?\\s*", "");
            trimmed = trimmed.replaceFirst("\\s*```$", "");
        }
        return trimmed.trim();
    }
}
