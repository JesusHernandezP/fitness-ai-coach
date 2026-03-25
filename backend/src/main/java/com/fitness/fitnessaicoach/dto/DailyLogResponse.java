package com.fitness.fitnessaicoach.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
public class DailyLogResponse {

    private UUID id;

    private LocalDate date;

    private Integer steps;

    private Double caloriesConsumed;

    private Double caloriesBurned;

    private UUID userId;
}
