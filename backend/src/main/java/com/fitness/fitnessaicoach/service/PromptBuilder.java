package com.fitness.fitnessaicoach.service;

import com.fitness.fitnessaicoach.dto.ai.AIAnalysisResponse;
import com.fitness.fitnessaicoach.dto.ai.AIMealSummaryResponse;
import com.fitness.fitnessaicoach.dto.ai.AIWorkoutSummaryResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class PromptBuilder {

    public String buildPrompt(AIAnalysisResponse analysis) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("You are an expert fitness and nutrition coach.")
                .append(" Provide concise, actionable advice in a friendly tone.\n\n")
                .append("Daily Log Summary:\n")
                .append("Date: ").append(formatDate(analysis.getDate())).append("\n")
                .append("Steps: ").append(formatNumber(analysis.getTotalSteps())).append("\n")
                .append("Meals logged: ").append(formatNumber(analysis.getTotalMeals())).append("\n")
                .append("Workouts logged: ").append(formatNumber(analysis.getTotalWorkoutSessions())).append("\n")
                .append("Calories consumed: ").append(formatDecimal(analysis.getTotalCaloriesConsumed())).append("\n")
                .append("Calories burned: ").append(formatDecimal(analysis.getTotalCaloriesBurned())).append("\n")
                .append("Calorie balance: ").append(formatDecimal(analysis.getCalorieBalance())).append("\n\n");

        appendGoal(prompt, analysis);
        appendBodyMetrics(prompt, analysis);
        appendMeals(prompt, analysis.getMeals());
        appendWorkouts(prompt, analysis.getWorkouts());

        prompt.append("\nReturn only the coaching advice text.");
        return prompt.toString();
    }

    private void appendGoal(StringBuilder prompt, AIAnalysisResponse analysis) {
        if (analysis.getGoalType() == null) {
            return;
        }

        prompt.append("Goal: ").append(analysis.getGoalType()).append("\n");
        if (analysis.getTargetCalories() != null) {
            prompt.append("Target calories: ").append(formatDecimal(BigDecimal.valueOf(analysis.getTargetCalories()))).append("\n");
        }
        if (analysis.getTargetWeight() != null) {
            prompt.append("Target weight: ").append(formatDecimal(BigDecimal.valueOf(analysis.getTargetWeight()))).append("\n");
        }
        prompt.append("\n");
    }

    private void appendBodyMetrics(StringBuilder prompt, AIAnalysisResponse analysis) {
        if (analysis.getLatestWeight() == null && analysis.getLatestBodyFat() == null && analysis.getLatestMuscleMass() == null) {
            return;
        }

        prompt.append("Latest body metrics (if available):\n");
        if (analysis.getLatestWeight() != null) {
            prompt.append("- Weight: ").append(formatDecimal(BigDecimal.valueOf(analysis.getLatestWeight()))).append("\n");
        }
        if (analysis.getLatestBodyFat() != null) {
            prompt.append("- Body fat: ").append(formatDecimal(BigDecimal.valueOf(analysis.getLatestBodyFat()))).append("\n");
        }
        if (analysis.getLatestMuscleMass() != null) {
            prompt.append("- Muscle mass: ").append(formatDecimal(BigDecimal.valueOf(analysis.getLatestMuscleMass()))).append("\n");
        }
        prompt.append("\n");
    }

    private void appendMeals(StringBuilder prompt, List<AIMealSummaryResponse> meals) {
        if (meals == null || meals.isEmpty()) {
            prompt.append("Meals: none logged\n\n");
            return;
        }

        prompt.append("Meals:\n");
        for (AIMealSummaryResponse meal : meals) {
            prompt.append("- ").append(meal.getMealType()).append(": ")
                    .append(formatNumber(meal.getTotalItems())).append(" items, ")
                    .append(formatDecimal(meal.getTotalCalories())).append(" calories\n");
        }
        prompt.append("\n");
    }

    private void appendWorkouts(StringBuilder prompt, List<AIWorkoutSummaryResponse> workouts) {
        if (workouts == null || workouts.isEmpty()) {
            prompt.append("Workouts: none logged\n\n");
            return;
        }

        prompt.append("Workouts:\n");
        for (AIWorkoutSummaryResponse workout : workouts) {
            prompt.append("- ")
                    .append(workout.getExerciseName() != null ? workout.getExerciseName() : "Unknown")
                    .append(": ")
                    .append(formatNumber(workout.getDuration())).append(" min, ")
                    .append(formatDecimal(workout.getCaloriesBurned())).append(" calories burned\n");
        }
        prompt.append("\n");
    }

    private String formatDate(Object date) {
        return date != null ? date.toString() : "unknown";
    }

    private String formatNumber(Integer number) {
        return String.valueOf(number != null ? number : 0);
    }

    private String formatDecimal(BigDecimal decimal) {
        if (decimal == null) {
            return "0";
        }
        return decimal.stripTrailingZeros().toPlainString();
    }
}
