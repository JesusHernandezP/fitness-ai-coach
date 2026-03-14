package com.fitness.fitnessaicoach.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class BodyMetricsRequest {

    @NotNull
    private UUID userId;

    @NotNull
    @Positive(message = "Weight must be greater than 0.")
    private Double weight;

    private Double bodyFat;

    private Double muscleMass;

    @NotNull
    private LocalDate date;
}
