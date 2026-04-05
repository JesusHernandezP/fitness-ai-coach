package com.fitness.fitnessaicoach.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class MealItemRequest {

    @NotNull
    private UUID mealId;

    private UUID foodId;

    @Size(max = 255, message = "Food name cannot exceed 255 characters.")
    private String foodName;

    @NotNull
    @Positive(message = "Quantity must be greater than 0.")
    private Double quantity;
}
