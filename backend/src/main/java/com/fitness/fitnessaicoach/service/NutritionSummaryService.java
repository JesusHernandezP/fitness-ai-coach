package com.fitness.fitnessaicoach.service;

import com.fitness.fitnessaicoach.domain.DailyLog;
import com.fitness.fitnessaicoach.domain.DietType;
import com.fitness.fitnessaicoach.domain.Goal;
import com.fitness.fitnessaicoach.domain.MealItem;
import com.fitness.fitnessaicoach.domain.User;
import com.fitness.fitnessaicoach.repository.DailyLogRepository;
import com.fitness.fitnessaicoach.repository.GoalRepository;
import com.fitness.fitnessaicoach.repository.MealItemRepository;
import com.fitness.fitnessaicoach.repository.UserRepository;
import com.fitness.fitnessaicoach.repository.WorkoutSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fitness.fitnessaicoach.dto.DailyNutritionSummaryResponse;

@Service
@RequiredArgsConstructor
public class NutritionSummaryService {

    private final UserRepository userRepository;
    private final GoalRepository goalRepository;
    private final DailyLogRepository dailyLogRepository;
    private final MealItemRepository mealItemRepository;
    private final WorkoutSessionRepository workoutSessionRepository;
    private final NutritionMath nutritionMath;

    @Transactional(readOnly = true)
    public DailyNutritionSummaryResponse buildForDate(UUID userId, LocalDate date) {
        User user = userRepository.findById(userId).orElseThrow();
        DailyLog dailyLog = dailyLogRepository.findByUserIdAndLogDate(userId, date).orElse(null);
        Goal goal = goalRepository.findTopByUserIdOrderByCreatedAtDescIdDesc(userId).orElse(null);

        List<MealItem> mealItems = dailyLog != null ? mealItemRepository.findAllByDailyLogId(dailyLog.getId()) : List.of();
        double consumedCalories = mealItems.stream()
                .mapToDouble(nutritionMath::caloriesFor)
                .sum();
        double consumedProtein = mealItems.stream()
                .mapToDouble(nutritionMath::proteinFor)
                .sum();
        double consumedCarbs = mealItems.stream()
                .mapToDouble(nutritionMath::carbsFor)
                .sum();
        double consumedFat = mealItems.stream()
                .mapToDouble(nutritionMath::fatFor)
                .sum();

        double targetCalories = goal != null && goal.getTargetCalories() != null ? goal.getTargetCalories() : 0.0;
        double targetProtein = goal != null && goal.getTargetProtein() != null ? goal.getTargetProtein() : 0.0;
        double targetCarbs = goal != null && goal.getTargetCarbs() != null ? goal.getTargetCarbs() : 0.0;
        double targetFat = goal != null && goal.getTargetFat() != null ? goal.getTargetFat() : 0.0;
        int steps = dailyLog != null && dailyLog.getSteps() != null ? dailyLog.getSteps() : 0;
        double caloriesBurned = calculateCaloriesBurned(dailyLog);

        return DailyNutritionSummaryResponse.builder()
                .date(date)
                .targetCalories(targetCalories)
                .targetProtein(targetProtein)
                .targetCarbs(targetCarbs)
                .targetFat(targetFat)
                .consumedCalories(consumedCalories)
                .consumedProtein(consumedProtein)
                .consumedCarbs(consumedCarbs)
                .consumedFat(consumedFat)
                .remainingCalories(Math.max(0.0, targetCalories - consumedCalories))
                .remainingProtein(Math.max(0.0, targetProtein - consumedProtein))
                .remainingCarbs(Math.max(0.0, targetCarbs - consumedCarbs))
                .remainingFat(Math.max(0.0, targetFat - consumedFat))
                .steps(steps)
                .caloriesBurned(caloriesBurned)
                .dietType((user.getDietType() != null ? user.getDietType() : DietType.STANDARD).name())
                .goalType(goal != null && goal.getGoalType() != null ? goal.getGoalType().name() : "UNKNOWN")
                .adherenceNotes(buildAdherenceNotes(user, goal, consumedCalories, consumedProtein, consumedCarbs, targetCalories, targetProtein, targetCarbs))
                .build();
    }

    private double calculateCaloriesBurned(DailyLog dailyLog) {
        if (dailyLog == null) {
            return 0.0;
        }

        double manualBurn = dailyLog.getCaloriesBurned() != null ? dailyLog.getCaloriesBurned() : 0.0;
        double workoutBurn = workoutSessionRepository.sumCaloriesBurnedByDailyLogId(dailyLog.getId()) != null
                ? workoutSessionRepository.sumCaloriesBurnedByDailyLogId(dailyLog.getId())
                : 0.0;

        return Math.max(manualBurn, workoutBurn);
    }

    private List<String> buildAdherenceNotes(
            User user,
            Goal goal,
            double consumedCalories,
            double consumedProtein,
            double consumedCarbs,
            double targetCalories,
            double targetProtein,
            double targetCarbs
    ) {
        List<String> notes = new ArrayList<>();
        if (goal == null) {
            notes.add("Completa tu perfil y objetivo para recibir recomendaciones más precisas.");
            return notes;
        }
        if (targetProtein > 0 && consumedProtein < targetProtein * 0.75) {
            notes.add("Proteina por debajo de lo ideal para tu objetivo actual.");
        }
        if (targetCalories > 0 && consumedCalories > targetCalories * 1.1) {
            notes.add("Tus calorias van por encima del objetivo diario.");
        } else if (targetCalories > 0 && consumedCalories < targetCalories * 0.6) {
            notes.add("Tus calorias van bastante por debajo del objetivo diario.");
        }
        if ((user.getDietType() != null ? user.getDietType() : DietType.STANDARD) == DietType.KETO
                && targetCarbs > 0
                && consumedCarbs > targetCarbs * 1.1) {
            notes.add("Estas superando el rango de carbohidratos recomendado para keto.");
        }
        if (notes.isEmpty()) {
            notes.add("Vas alineado con tu plan del dia.");
        }
        return notes;
    }
}
