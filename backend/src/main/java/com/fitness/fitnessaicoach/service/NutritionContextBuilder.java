package com.fitness.fitnessaicoach.service;

import com.fitness.fitnessaicoach.domain.BodyMetrics;
import com.fitness.fitnessaicoach.domain.DailyLog;
import com.fitness.fitnessaicoach.domain.Goal;
import com.fitness.fitnessaicoach.domain.MealItem;
import com.fitness.fitnessaicoach.domain.User;
import com.fitness.fitnessaicoach.repository.BodyMetricsRepository;
import com.fitness.fitnessaicoach.repository.DailyLogRepository;
import com.fitness.fitnessaicoach.repository.GoalRepository;
import com.fitness.fitnessaicoach.repository.MealItemRepository;
import com.fitness.fitnessaicoach.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NutritionContextBuilder {

    private final UserRepository userRepository;
    private final GoalRepository goalRepository;
    private final DailyLogRepository dailyLogRepository;
    private final BodyMetricsRepository bodyMetricsRepository;
    private final MealItemRepository mealItemRepository;

    public PromptBuilder.NutritionContext build(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow();
        Goal latestGoal = goalRepository.findTopByUserIdOrderByCreatedAtDescIdDesc(userId).orElse(null);
        DailyLog latestDailyLog = dailyLogRepository.findTopByUserIdOrderByLogDateDescIdDesc(userId).orElse(null);
        BodyMetrics latestBodyMetrics = bodyMetricsRepository.findTopByUserIdOrderByDateDescIdDesc(userId).orElse(null);

        List<MealItem> mealItems = latestDailyLog != null
                ? mealItemRepository.findAllByDailyLogId(latestDailyLog.getId())
                : List.of();

        double consumedCalories = latestDailyLog != null && latestDailyLog.getCaloriesConsumed() != null
                ? latestDailyLog.getCaloriesConsumed()
                : 0.0;
        double consumedProtein = mealItems.stream()
                .mapToDouble(item -> item.getFood().getProtein() * item.getQuantity())
                .sum();
        double consumedCarbs = mealItems.stream()
                .mapToDouble(item -> item.getFood().getCarbs() * item.getQuantity())
                .sum();
        double consumedFat = mealItems.stream()
                .mapToDouble(item -> item.getFood().getFat() * item.getQuantity())
                .sum();

        double targetCalories = latestGoal != null && latestGoal.getTargetCalories() != null
                ? latestGoal.getTargetCalories()
                : 0.0;
        double targetProtein = latestGoal != null && latestGoal.getTargetProtein() != null
                ? latestGoal.getTargetProtein()
                : 0.0;
        double targetCarbs = latestGoal != null && latestGoal.getTargetCarbs() != null
                ? latestGoal.getTargetCarbs()
                : 0.0;
        double targetFat = latestGoal != null && latestGoal.getTargetFat() != null
                ? latestGoal.getTargetFat()
                : 0.0;

        Object weight = latestBodyMetrics != null && latestBodyMetrics.getWeight() != null
                ? latestBodyMetrics.getWeight()
                : user.getWeightKg() != null ? user.getWeightKg() : "unknown";

        return new PromptBuilder.NutritionContext(
                latestGoal != null && latestGoal.getGoalType() != null ? latestGoal.getGoalType().name() : "UNKNOWN",
                weight,
                user.getActivityLevel() != null ? user.getActivityLevel().name() : "unknown",
                targetCalories,
                targetProtein,
                targetCarbs,
                targetFat,
                consumedCalories,
                consumedProtein,
                consumedCarbs,
                consumedFat,
                Math.max(0.0, targetCalories - consumedCalories),
                Math.max(0.0, targetProtein - consumedProtein),
                Math.max(0.0, targetCarbs - consumedCarbs),
                Math.max(0.0, targetFat - consumedFat),
                latestGoal != null || latestDailyLog != null
        );
    }
}
