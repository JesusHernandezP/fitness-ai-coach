package com.fitness.fitnessaicoach.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class MealItemRequest {

    @NotNull
    private UUID mealId;

    @NotNull
    private UUID foodId;

    @NotNull
    private Double quantity;
}
