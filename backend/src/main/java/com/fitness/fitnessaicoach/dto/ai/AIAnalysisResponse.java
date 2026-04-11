package com.fitness.fitnessaicoach.dto.ai;

import com.fitness.fitnessaicoach.domain.ActivityLevel;
import com.fitness.fitnessaicoach.domain.UserGoalType;
import com.fitness.fitnessaicoach.domain.UserSex;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@ToString
public class AIAnalysisResponse {

    private UUID dailyLogId;
    private UUID userId;
    private LocalDate date;
    private Integer totalSteps;
    private Integer totalMeals;
    private Integer totalWorkoutSessions;
    private BigDecimal totalCaloriesConsumed;
    private BigDecimal totalCaloriesBurned;
    private BigDecimal calorieBalance;
    private UserGoalType goalType;
    private Double targetWeight;
    private Double targetCalories;
    private Double targetProtein;
    private Double targetCarbs;
    private Double targetFat;
    private UserSex sex;
    private ActivityLevel activityLevel;
    private Double latestWeight;
    private Double latestBodyFat;
    private Double latestMuscleMass;
    private List<AIMealSummaryResponse> meals;
    private List<AIWorkoutSummaryResponse> workouts;
}
