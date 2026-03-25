package com.fitness.fitnessaicoach.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class WorkoutSessionResponse {

    private UUID id;

    private UUID dailyLogId;

    private UUID exerciseId;

    private Integer sets;

    private Integer reps;

    private Integer duration;

    private Double caloriesBurned;
}
