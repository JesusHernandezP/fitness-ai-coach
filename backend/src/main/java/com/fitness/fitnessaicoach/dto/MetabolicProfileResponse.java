package com.fitness.fitnessaicoach.dto;

import com.fitness.fitnessaicoach.domain.ActivityLevel;
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
    private UserSex sex;
    private ActivityLevel activityLevel;
}
