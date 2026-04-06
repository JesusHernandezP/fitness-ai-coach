package com.fitness.fitnessaicoach.dto;

import com.fitness.fitnessaicoach.domain.ActivityLevel;
import com.fitness.fitnessaicoach.domain.UserSex;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetabolicProfileRequest {

    @NotNull(message = "Age is required.")
    @Positive(message = "Age must be greater than 0.")
    private Integer age;

    @NotNull(message = "Height is required.")
    @Positive(message = "Height must be greater than 0.")
    private Double heightCm;

    @NotNull(message = "Sex is required.")
    private UserSex sex;

    @NotNull(message = "Activity level is required.")
    private ActivityLevel activityLevel;
}
