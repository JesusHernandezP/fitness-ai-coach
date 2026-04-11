package com.fitness.fitnessaicoach.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class MealItemRequest {

    @NotNull
    private UUID mealId;

    private UUID foodId;

    private String foodName;

    @NotNull
    @Positive(message = "Quantity must be greater than 0.")
    private Double quantity;
}
