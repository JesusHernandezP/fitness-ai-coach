package com.fitness.fitnessaicoach.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FoodRequest {

    @NotBlank
    private String name;

    private double calories;

    private double protein;

    private double carbs;

    private double fat;
}
