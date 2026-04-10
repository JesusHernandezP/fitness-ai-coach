package com.fitness.fitnessaicoach.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class WorkoutSessionRequest {

    @NotNull
    private UUID dailyLogId;

    @NotNull
    private UUID exerciseId;

    @NotNull
    @Positive(message = "Sets must be greater than 0.")
    private Integer sets;

    @NotNull
    @Positive(message = "Reps must be greater than 0.")
    private Integer reps;

    @NotNull
    @Positive(message = "Duration must be greater than 0.")
    private Integer duration;

    @PositiveOrZero(message = "Calories burned cannot be negative.")
    private Double caloriesBurned;
}
