package com.fitness.fitnessaicoach.dto;

import com.fitness.fitnessaicoach.domain.MealType;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class MealResponse {

    private UUID id;

    private MealType mealType;

    private UUID dailyLogId;
}
