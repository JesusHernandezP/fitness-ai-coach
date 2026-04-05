package com.fitness.fitnessaicoach.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class BodyMetricsRequest {

    @NotNull
    @Positive(message = "Weight must be greater than 0.")
    private Double weight;

    @NotNull
    private LocalDate date;
}
