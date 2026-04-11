package com.fitness.fitnessaicoach.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class FoodResponse {

    private UUID id;

    private String name;

    private double calories;

    private double protein;

    private double carbs;

    private double fat;
}
