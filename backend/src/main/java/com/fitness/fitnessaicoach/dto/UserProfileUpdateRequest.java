package com.fitness.fitnessaicoach.dto;

import com.fitness.fitnessaicoach.domain.ActivityLevel;
import com.fitness.fitnessaicoach.domain.UserSex;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
public class UserProfileUpdateRequest {

    @Min(value = 15, message = "Age must be at least 15.")
    @Max(value = 80, message = "Age cannot be greater than 80.")
    private Integer age;

    @DecimalMin(value = "120.0", message = "Height must be at least 120 cm.")
    @DecimalMax(value = "220.0", message = "Height cannot be greater than 220 cm.")
    private Double heightCm;

    private UserSex sex;

    private ActivityLevel activityLevel;
}
