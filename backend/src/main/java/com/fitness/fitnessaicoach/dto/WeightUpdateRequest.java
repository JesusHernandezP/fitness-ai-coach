package com.fitness.fitnessaicoach.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WeightUpdateRequest {

    @NotNull
    private Double weightKg;
}
