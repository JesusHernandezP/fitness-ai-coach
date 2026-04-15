package com.fitness.fitnessaicoach.dto;

import java.time.LocalDate;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DailyLogResponse {

    private UUID id;

    private LocalDate date;

    private Integer steps;

    private Double caloriesConsumed;

    private Double caloriesBurned;

    private Double weightKg;

    private UUID userId;
}
