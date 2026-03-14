package com.fitness.fitnessaicoach.dto;

import com.fitness.fitnessaicoach.domain.MealType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class MealRequest {

    @NotNull
    private MealType mealType;

    @NotNull
    private UUID dailyLogId;
}
