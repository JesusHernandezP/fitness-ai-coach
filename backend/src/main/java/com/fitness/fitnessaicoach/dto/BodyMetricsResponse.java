package com.fitness.fitnessaicoach.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
public class BodyMetricsResponse {

    private UUID id;

    private UUID userId;

    private Double weight;

<<<<<<< HEAD
=======
    private Double bodyFat;

    private Double muscleMass;

>>>>>>> main
    private LocalDate date;
}
