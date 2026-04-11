package com.fitness.fitnessaicoach.dto;

import com.fitness.fitnessaicoach.domain.UserGoalType;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class GoalResponse {

    private UUID id;
    private UserGoalType goalType;
    private Double targetWeight;
    private Double targetCalories;
    private Double targetProtein;
    private Double targetCarbs;
    private Double targetFat;
    private UUID userId;
}
