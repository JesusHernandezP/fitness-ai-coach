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
            return fallbackEstimate(foodName);
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

    private AIFoodEstimateResponse fallbackEstimate(String foodName) {
        String normalized = foodName == null ? "food" : foodName.trim().toLowerCase();

        double calories = 120.0;
        double protein = 6.0;
        double carbs = 12.0;
        double fat = 4.0;

        if (normalized.contains("chicken") || normalized.contains("turkey") || normalized.contains("tuna")) {
            calories = 165.0;
            protein = 31.0;
            carbs = 0.0;
            fat = 3.6;
        } else if (normalized.contains("rice") || normalized.contains("pasta") || normalized.contains("bread")) {
            calories = 130.0;
            protein = 2.7;
            carbs = 28.0;
            fat = 0.3;
        } else if (normalized.contains("egg")) {
            calories = 78.0;
            protein = 6.0;
            carbs = 0.6;
            fat = 5.0;
        } else if (normalized.contains("banana") || normalized.contains("apple")) {
            calories = 95.0;
            protein = 0.5;
            carbs = 25.0;
            fat = 0.3;
        } else if (normalized.contains("protein shake")) {
            calories = 160.0;
            protein = 25.0;
            carbs = 8.0;
            fat = 3.0;
        }

        AIFoodEstimateResponse response = new AIFoodEstimateResponse();
        response.setName(foodName);
        response.setCalories(calories);
        response.setProtein(protein);
        response.setCarbs(carbs);
        response.setFat(fat);
        return response;
    }
}
