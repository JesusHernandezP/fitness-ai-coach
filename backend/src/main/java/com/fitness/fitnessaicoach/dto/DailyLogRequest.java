package com.fitness.fitnessaicoach.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class DailyLogRequest {

    @NotNull
    private LocalDate logDate;

    private Integer steps;

    private Double caloriesConsumed;

    private Double caloriesBurned;

    @NotNull
    private UUID userId;
}
