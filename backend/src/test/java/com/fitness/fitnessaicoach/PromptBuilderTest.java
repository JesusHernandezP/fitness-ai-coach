package com.fitness.fitnessaicoach;

import com.fitness.fitnessaicoach.domain.ActivityLevel;
import com.fitness.fitnessaicoach.domain.UserGoalType;
import com.fitness.fitnessaicoach.domain.UserSex;
import com.fitness.fitnessaicoach.dto.ai.AIAnalysisResponse;
import com.fitness.fitnessaicoach.dto.ai.AIMealSummaryResponse;
import com.fitness.fitnessaicoach.dto.ai.AIWorkoutSummaryResponse;
import com.fitness.fitnessaicoach.service.PromptBuilder;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PromptBuilderTest {

    private final PromptBuilder promptBuilder = new PromptBuilder();

    @Test
    void buildPromptShouldIncludeStructuredDailySummarySections() {
        AIAnalysisResponse analysis = AIAnalysisResponse.builder()
                .dailyLogId(UUID.randomUUID())
                .date(LocalDate.of(2026, 4, 6))
                .goalType(UserGoalType.LOSE_WEIGHT)
                .targetWeight(78.0)
                .targetCalories(1800.0)
                .targetProtein(140.0)
                .targetCarbs(160.0)
                .targetFat(55.0)
                .latestWeight(82.0)
                .sex(UserSex.MALE)
                .activityLevel(ActivityLevel.MODERATE)
                .totalMeals(2)
                .totalWorkoutSessions(4)
                .totalCaloriesConsumed(BigDecimal.valueOf(1800))
                .totalCaloriesBurned(BigDecimal.valueOf(2100))
                .calorieBalance(BigDecimal.valueOf(-300))
                .totalSteps(8500)
                .meals(List.of(
                        AIMealSummaryResponse.builder()
                                .mealType("SNACK")
                                .totalItems(2)
                                .totalCalories(BigDecimal.valueOf(320))
                                .build(),
                        AIMealSummaryResponse.builder()
                                .mealType("LUNCH")
                                .totalItems(3)
                                .totalCalories(BigDecimal.valueOf(740))
                                .build()
                ))
                .workouts(List.of(
                        AIWorkoutSummaryResponse.builder()
                                .exerciseName("Bench Press")
                                .duration(30)
                                .caloriesBurned(BigDecimal.valueOf(220))
                                .build(),
                        AIWorkoutSummaryResponse.builder()
                                .exerciseName("Lat Pulldown")
                                .duration(25)
                                .caloriesBurned(BigDecimal.valueOf(180))
                                .build()
                ))
                .build();

        String prompt = promptBuilder.buildPrompt(analysis);

        assertThat(prompt).contains("User daily summary:");
        assertThat(prompt).contains("Calories consumed: 1800");
        assertThat(prompt).contains("Calories burned: 2100");
        assertThat(prompt).contains("Calorie balance: -300");
        assertThat(prompt).contains("Steps: 8500");
        assertThat(prompt).contains("Meals:");
        assertThat(prompt).contains("- Snack: 2 item(s), 320 kcal");
        assertThat(prompt).contains("- Lunch: 3 item(s), 740 kcal");
        assertThat(prompt).contains("Workout:");
        assertThat(prompt).contains("- Bench Press, duration 30 min, calories 220 kcal");
        assertThat(prompt).contains("- Lat Pulldown, duration 25 min, calories 180 kcal");
        assertThat(prompt).contains("Goal:");
        assertThat(prompt).contains("Goal type: LOSE_WEIGHT");
        assertThat(prompt).contains("Target calories: 1800.0");
        assertThat(prompt).contains("Body metrics:");
        assertThat(prompt).contains("Weight: 82.0");
        assertThat(prompt).contains("Do not hallucinate food details");
    }
}
