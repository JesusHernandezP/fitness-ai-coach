package com.fitness.fitnessaicoach.dto;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DailyLogRequest {

    @NotNull
    private LocalDate logDate;

    private Integer steps;

    private Double caloriesConsumed;

    private Double caloriesBurned;

    private Double weightKg;

    @NotNull
    private UUID userId;
}
