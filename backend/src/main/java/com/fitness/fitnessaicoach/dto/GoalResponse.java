package com.fitness.fitnessaicoach.dto;

import com.fitness.fitnessaicoach.domain.GoalType;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class GoalResponse {

    private UUID id;

    private GoalType goalType;

    private Double targetWeight;

    private Double targetCalories;

    private UUID userId;
}
