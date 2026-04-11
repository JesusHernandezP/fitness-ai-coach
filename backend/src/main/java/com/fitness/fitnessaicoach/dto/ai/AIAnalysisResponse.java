package com.fitness.fitnessaicoach.dto.ai;

<<<<<<< HEAD
import com.fitness.fitnessaicoach.domain.ActivityLevel;
import com.fitness.fitnessaicoach.domain.UserGoalType;
import com.fitness.fitnessaicoach.domain.UserSex;
=======
import com.fitness.fitnessaicoach.domain.UserGoalType;
>>>>>>> main
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
<<<<<<< HEAD
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
    private Double latestWeight;
    private UserSex sex;
    private ActivityLevel activityLevel;
    private List<AIMealSummaryResponse> meals;
=======

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

    private Double latestWeight;

    private Double latestBodyFat;

    private Double latestMuscleMass;

    private List<AIMealSummaryResponse> meals;

>>>>>>> main
    private List<AIWorkoutSummaryResponse> workouts;
}
