package com.fitness.fitnessaicoach.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class MealItemResponse {

    private UUID id;

    private UUID mealId;

    private UUID foodId;

    private Double quantity;

    private Double calculatedCalories;
}
