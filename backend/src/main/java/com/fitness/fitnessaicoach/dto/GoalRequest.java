package com.fitness.fitnessaicoach.dto;

import com.fitness.fitnessaicoach.domain.UserGoalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

<<<<<<< HEAD
=======
import java.util.UUID;

>>>>>>> main
@Getter
@Setter
public class GoalRequest {

    @NotNull
<<<<<<< HEAD
    private UserGoalType goalType;

    @Positive(message = "Target weight must be greater than 0.")
    private Double targetWeight;
=======
    private UUID userId;

    @NotNull
    private UserGoalType goalType;

    @NotNull
    @Positive(message = "Target weight must be greater than 0.")
    private Double targetWeight;

    @NotNull
    @Positive(message = "Target calories must be greater than 0.")
    private Double targetCalories;
>>>>>>> main
}
