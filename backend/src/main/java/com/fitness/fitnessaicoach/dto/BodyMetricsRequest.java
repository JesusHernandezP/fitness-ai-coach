package com.fitness.fitnessaicoach.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.PositiveOrZero;
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

    @DecimalMin(value = "0", inclusive = true, message = "Body fat must be at least 0.")
    @DecimalMax(value = "100", inclusive = true, message = "Body fat cannot exceed 100.")
    private Double bodyFat;

    @PositiveOrZero(message = "Muscle mass cannot be negative.")
    private Double muscleMass;

    @NotNull
    private LocalDate date;
}
