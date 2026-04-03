package com.fitness.fitnessaicoach.dto;

import com.fitness.fitnessaicoach.domain.UserGoalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoalRequest {

    @NotNull
    private UserGoalType goalType;

    @Positive(message = "Target weight must be greater than 0.")
    private Double targetWeight;

    @NotNull
    @Positive(message = "Target calories must be greater than 0.")
    private Double targetCalories;
}
