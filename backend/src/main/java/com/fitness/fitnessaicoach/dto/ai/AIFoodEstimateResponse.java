package com.fitness.fitnessaicoach.dto.ai;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AIFoodEstimateResponse {

    @NotBlank
    private String name;

    private double calories;

    private double protein;

    private double carbs;

    private double fat;
}
