package com.fitness.fitnessaicoach.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FoodRequest {

    @NotBlank(message = "Food name is required.")
    private String name;

    @PositiveOrZero(message = "Calories cannot be negative.")
    private double calories;

    @PositiveOrZero(message = "Protein cannot be negative.")
    private double protein;

    @PositiveOrZero(message = "Carbs cannot be negative.")
    private double carbs;

    @PositiveOrZero(message = "Fat cannot be negative.")
    private double fat;
}
