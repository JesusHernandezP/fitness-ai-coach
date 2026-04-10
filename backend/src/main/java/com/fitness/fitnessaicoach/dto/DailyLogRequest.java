package com.fitness.fitnessaicoach.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class DailyLogRequest {

    @NotNull
    private LocalDate logDate;

    @PositiveOrZero(message = "Steps cannot be negative.")
    private Integer steps;

    @PositiveOrZero(message = "Calories consumed cannot be negative.")
    private Double caloriesConsumed;

    @PositiveOrZero(message = "Calories burned cannot be negative.")
    private Double caloriesBurned;

    @NotNull
    private UUID userId;
}
