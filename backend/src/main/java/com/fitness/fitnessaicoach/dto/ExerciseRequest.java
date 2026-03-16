package com.fitness.fitnessaicoach.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExerciseRequest {

    @NotBlank
    private String name;

    private String muscleGroup;

    private String equipment;

    private String description;
}