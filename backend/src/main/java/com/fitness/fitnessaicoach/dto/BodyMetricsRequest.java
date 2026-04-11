package com.fitness.fitnessaicoach.dto;

<<<<<<< HEAD
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotNull;
=======
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Positive;
>>>>>>> main
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
<<<<<<< HEAD
=======
import java.util.UUID;
>>>>>>> main

@Getter
@Setter
public class BodyMetricsRequest {

    @NotNull
<<<<<<< HEAD
    @Positive(message = "Weight must be greater than 0.")
    private Double weight;

=======
    private UUID userId;

    @NotNull
    @Positive(message = "Weight must be greater than 0.")
    private Double weight;

    @DecimalMin(value = "0", inclusive = true, message = "Body fat must be at least 0.")
    @DecimalMax(value = "100", inclusive = true, message = "Body fat cannot exceed 100.")
    private Double bodyFat;

    @PositiveOrZero(message = "Muscle mass cannot be negative.")
    private Double muscleMass;

>>>>>>> main
    @NotNull
    private LocalDate date;
}
