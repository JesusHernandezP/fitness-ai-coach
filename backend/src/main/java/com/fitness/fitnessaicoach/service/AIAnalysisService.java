package com.fitness.fitnessaicoach.service;

import com.fitness.fitnessaicoach.domain.BodyMetrics;
import com.fitness.fitnessaicoach.domain.DailyLog;
import com.fitness.fitnessaicoach.domain.Goal;
import com.fitness.fitnessaicoach.domain.Meal;
import com.fitness.fitnessaicoach.domain.MealItem;
import com.fitness.fitnessaicoach.domain.MealType;
import com.fitness.fitnessaicoach.domain.WorkoutSession;
import com.fitness.fitnessaicoach.domain.User;
import com.fitness.fitnessaicoach.dto.ai.AIAnalysisResponse;
import com.fitness.fitnessaicoach.dto.ai.AIMealSummaryResponse;
import com.fitness.fitnessaicoach.dto.ai.AIWorkoutSummaryResponse;
import com.fitness.fitnessaicoach.exception.DailyLogNotFoundException;
import com.fitness.fitnessaicoach.repository.BodyMetricsRepository;
import com.fitness.fitnessaicoach.repository.DailyLogRepository;
import com.fitness.fitnessaicoach.repository.GoalRepository;
import com.fitness.fitnessaicoach.repository.MealItemRepository;
import com.fitness.fitnessaicoach.repository.MealRepository;
import com.fitness.fitnessaicoach.repository.WorkoutSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AIAnalysisService {

    private static final int CALORIE_SCALE = 2;
    private static final RoundingMode CALORIE_ROUNDING = RoundingMode.HALF_UP;

    private final DailyLogRepository dailyLogRepository;
    private final MealRepository mealRepository;
    private final MealItemRepository mealItemRepository;
    private final WorkoutSessionRepository workoutSessionRepository;
    private final GoalRepository goalRepository;
    private final BodyMetricsRepository bodyMetricsRepository;

    public AIAnalysisResponse getDailyLogAiAnalysis(UUID dailyLogId) {
        DailyLog dailyLog = dailyLogRepository.findById(dailyLogId)
                .orElseThrow(() -> new DailyLogNotFoundException("Daily log not found."));

        User user = dailyLog.getUser();
        UUID userId = user != null ? user.getId() : null;

        BigDecimal totalCaloriesConsumed = toBigDecimalOrZero(mealItemRepository.sumCalculatedCaloriesByDailyLogId(dailyLog.getId()));
        BigDecimal totalCaloriesBurned = toBigDecimalOrZero(workoutSessionRepository.sumCaloriesBurnedByDailyLogId(dailyLog.getId()));
        BigDecimal calorieBalance = totalCaloriesConsumed.subtract(totalCaloriesBurned)
                .setScale(CALORIE_SCALE, CALORIE_ROUNDING);

        Goal goal = userId != null
                ? goalRepository.findTopByUserIdOrderByCreatedAtDescIdDesc(userId).orElse(null)
                : null;

        BodyMetrics latestBodyMetrics = userId != null
                ? bodyMetricsRepository.findTopByUserIdOrderByDateDescIdDesc(userId).orElse(null)
                : null;

        return AIAnalysisResponse.builder()
                .dailyLogId(dailyLog.getId())
                .userId(userId)
                .date(dailyLog.getLogDate())
                .totalSteps(dailyLog.getSteps() != null ? dailyLog.getSteps() : 0)
                .totalMeals(toIntSafe(mealRepository.countByDailyLogId(dailyLogId)))
                .totalWorkoutSessions(toIntSafe(workoutSessionRepository.countByDailyLogId(dailyLogId)))
                .totalCaloriesConsumed(totalCaloriesConsumed)
                .totalCaloriesBurned(totalCaloriesBurned)
                .calorieBalance(calorieBalance)
                .goalType(goal != null ? goal.getGoalType() : null)
                .targetWeight(goal != null ? goal.getTargetWeight() : null)
                .targetCalories(goal != null ? goal.getTargetCalories() : null)
                .latestWeight(latestBodyMetrics != null ? latestBodyMetrics.getWeight() : null)
                .meals(buildMealSummaries(dailyLogId))
                .workouts(buildWorkoutSummaries(dailyLogId))
                .build();
    }

    private List<AIMealSummaryResponse> buildMealSummaries(UUID dailyLogId) {
        List<Meal> meals = mealRepository.findByDailyLogId(dailyLogId);
        List<MealItem> mealItems = mealItemRepository.findAllByDailyLogId(dailyLogId);

        if (meals.isEmpty()) {
            return List.of();
        }

        Map<MealType, Integer> totalItemsByMealType = new EnumMap<>(MealType.class);
        Map<MealType, BigDecimal> totalCaloriesByMealType = new EnumMap<>(MealType.class);

        for (Meal meal : meals) {
            if (meal.getMealType() != null) {
                totalItemsByMealType.putIfAbsent(meal.getMealType(), 0);
                totalCaloriesByMealType.putIfAbsent(meal.getMealType(), BigDecimal.ZERO.setScale(CALORIE_SCALE, CALORIE_ROUNDING));
            }
        }

        for (MealItem mealItem : mealItems) {
            MealType mealType = mealItem.getMeal() != null && mealItem.getMeal().getMealType() != null
                    ? mealItem.getMeal().getMealType()
                    : null;

            if (mealType != null) {
                totalItemsByMealType.merge(mealType, 1, Integer::sum);
                BigDecimal previousCalories = totalCaloriesByMealType.getOrDefault(mealType, BigDecimal.ZERO);
                totalCaloriesByMealType.put(
                        mealType,
                        previousCalories.add(toBigDecimalOrZero(mealItem.getCalculatedCalories())));
            }
        }

        List<AIMealSummaryResponse> summaries = new ArrayList<>();

        for (MealType mealType : MealType.values()) {
            Integer totalItems = totalItemsByMealType.get(mealType);
            if (totalItems == null) {
                continue;
            }

            summaries.add(AIMealSummaryResponse.builder()
                    .mealType(mealType.name())
                    .totalItems(totalItems)
                    .totalCalories(totalCaloriesByMealType.getOrDefault(mealType, BigDecimal.ZERO).setScale(CALORIE_SCALE, CALORIE_ROUNDING))
                    .build());
        }

        return summaries;
    }

    private List<AIWorkoutSummaryResponse> buildWorkoutSummaries(UUID dailyLogId) {
        return workoutSessionRepository.findByDailyLogId(dailyLogId).stream()
                .map(session -> AIWorkoutSummaryResponse.builder()
                        .exerciseName(session.getExercise() != null ? session.getExercise().getName() : null)
                        .duration(session.getDuration())
                        .caloriesBurned(toBigDecimalOrZero(session.getCaloriesBurned()))
                        .build())
                .toList()
                .stream()
                .sorted(Comparator.comparing(AIWorkoutSummaryResponse::getExerciseName, Comparator.nullsLast(String::compareTo)))
                .toList();
    }

    private int toIntSafe(long value) {
        if (value > Integer.MAX_VALUE) {
            throw new IllegalStateException("Daily log aggregate exceeds supported integer range.");
        }
        return (int) value;
    }

    private BigDecimal toBigDecimalOrZero(Double value) {
        return BigDecimal.valueOf(value != null ? value : 0)
                .setScale(CALORIE_SCALE, CALORIE_ROUNDING);
    }
}
