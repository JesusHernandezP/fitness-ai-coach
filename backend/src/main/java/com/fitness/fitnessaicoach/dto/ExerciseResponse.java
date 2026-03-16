package com.fitness.fitnessaicoach.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ExerciseResponse {

    private UUID id;

    private String name;

    private String muscleGroup;

    private String equipment;

    private String description;
}