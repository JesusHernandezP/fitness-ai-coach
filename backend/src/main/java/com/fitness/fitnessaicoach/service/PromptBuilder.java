package com.fitness.fitnessaicoach.service;

import com.fitness.fitnessaicoach.dto.ai.AIAnalysisResponse;
import com.fitness.fitnessaicoach.dto.ai.AIMealSummaryResponse;
import com.fitness.fitnessaicoach.dto.ai.AIWorkoutSummaryResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class PromptBuilder {

    public String buildPrompt(AIAnalysisResponse analysis) {
        return """
                You are a professional fitness coach.

                User daily summary:
                Calories consumed: %s
                Calories burned: %s
                Calorie balance: %s
                Steps: %s

                Meals:
                %s

                Workout:
                %s

                Goal:
                Goal type: %s
                Target calories: %s
                Target protein: %s
                Target carbs: %s
                Target fat: %s
                Target weight: %s

                Body metrics:
                Weight: %s
                Sex: %s
                Activity level: %s

                Give short, actionable advice in 2-3 sentences.
                Do not hallucinate food details.
                """.formatted(
                analysis.getTotalCaloriesConsumed(),
                analysis.getTotalCaloriesBurned(),
                analysis.getCalorieBalance(),
                analysis.getTotalSteps(),
                formatMeals(analysis.getMeals()),
                formatWorkouts(analysis.getWorkouts()),
                analysis.getGoalType(),
                analysis.getTargetCalories(),
                analysis.getTargetProtein(),
                analysis.getTargetCarbs(),
                analysis.getTargetFat(),
                analysis.getTargetWeight(),
                analysis.getLatestWeight(),
                analysis.getSex(),
                analysis.getActivityLevel()
        );
    }

    public String buildChatPrompt(ChatPromptContext context, String conversationHistory, String latestUserMessage) {
        return """
                You are Fitness AI Coach.

                User context:
                Goal type: %s
                Target calories: %s
                Target protein: %s
                Target carbs: %s
                Target fat: %s
                Calories consumed today: %s
                Calories burned today: %s
                Current calorie balance: %s
                Steps today: %s
                Latest weight: %s

                Recent conversation:
                %s

                Latest user message:
                %s

                Answer briefly, practically, and only with information supported by the known context.
                """.formatted(
                context.goalType(),
                context.targetCalories(),
                context.targetProtein(),
                context.targetCarbs(),
                context.targetFat(),
                context.caloriesConsumedToday(),
                context.caloriesBurnedToday(),
                context.calorieBalanceToday(),
                context.stepsToday(),
                context.latestWeight(),
                conversationHistory,
                latestUserMessage
        );
    }

    public record ChatPromptContext(
            String goalType,
            Object targetCalories,
            Object targetProtein,
            Object targetCarbs,
            Object targetFat,
            double caloriesConsumedToday,
            double caloriesBurnedToday,
            double calorieBalanceToday,
            int stepsToday,
            Object latestWeight
    ) {
    }

    private String formatMeals(List<AIMealSummaryResponse> meals) {
        if (meals == null || meals.isEmpty()) {
            return "No meals recorded.";
        }

        return meals.stream()
                .map(meal -> "- " + capitalize(meal.getMealType()) + ": " + meal.getTotalItems()
                        + " item(s), " + meal.getTotalCalories() + " kcal")
                .reduce((left, right) -> left + System.lineSeparator() + right)
                .orElse("No meals recorded.");
    }

    private String formatWorkouts(List<AIWorkoutSummaryResponse> workouts) {
        if (workouts == null || workouts.isEmpty()) {
            return "No workout sessions recorded.";
        }

        return workouts.stream()
                .map(workout -> "- " + workout.getExerciseName() + ", duration " + workout.getDuration()
                        + " min, calories " + workout.getCaloriesBurned() + " kcal")
                .reduce((left, right) -> left + System.lineSeparator() + right)
                .orElse("No workout sessions recorded.");
    }

    private String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return "Unknown";
        }

        String normalized = value.toLowerCase(Locale.ROOT);
        return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
    }
}
