package com.fitness.fitnessaicoach.dto;

import com.fitness.fitnessaicoach.domain.ActivityLevel;
import com.fitness.fitnessaicoach.domain.DietType;
import com.fitness.fitnessaicoach.domain.UserGoalType;
import com.fitness.fitnessaicoach.domain.UserSex;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetabolicProfileResponse {

    private UUID userId;
    private Integer age;
    private Double heightCm;
    private Double weightKg;
    private UserSex sex;
    private ActivityLevel activityLevel;
    private DietType dietType;
    private UserGoalType goalType;
    private Double targetCalories;
    private Double targetProtein;
    private Double targetCarbs;
    private Double targetFat;
}
