package com.fitness.fitnessaicoach.dto.ai;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class AIWorkoutSummaryResponse {

    private String exerciseName;

    private Integer duration;

    private BigDecimal caloriesBurned;
}
