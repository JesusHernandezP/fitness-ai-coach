package com.fitness.fitnessaicoach.service;

import com.fitness.fitnessaicoach.dto.ai.AIAnalysisResponse;
<<<<<<< HEAD
import com.fitness.fitnessaicoach.dto.ai.AIMealSummaryResponse;
import com.fitness.fitnessaicoach.dto.ai.AIWorkoutSummaryResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

=======
import org.springframework.stereotype.Component;

>>>>>>> main
@Component
public class PromptBuilder {

    public String buildPrompt(AIAnalysisResponse analysis) {
<<<<<<< HEAD
        String goalType = analysis.getGoalType() != null ? analysis.getGoalType().name() : "UNKNOWN";
        String targetCalories = formatNullableNumber(analysis.getTargetCalories());
        String targetProtein = formatNullableNumber(analysis.getTargetProtein());
        String targetCarbs = formatNullableNumber(analysis.getTargetCarbs());
        String targetFat = formatNullableNumber(analysis.getTargetFat());
        String caloriesConsumed = formatBigDecimal(analysis.getTotalCaloriesConsumed());
        String caloriesBurned = formatBigDecimal(analysis.getTotalCaloriesBurned());
        String calorieBalance = formatBigDecimal(analysis.getCalorieBalance());
        String steps = analysis.getTotalSteps() != null ? String.valueOf(analysis.getTotalSteps()) : "0";
        String latestWeight = formatNullableNumber(analysis.getLatestWeight());
        String targetWeight = formatNullableNumber(analysis.getTargetWeight());
        String sex = analysis.getSex() != null ? analysis.getSex().name() : "unknown";
        String activityLevel = analysis.getActivityLevel() != null ? analysis.getActivityLevel().name() : "unknown";
        String workoutCount = analysis.getTotalWorkoutSessions() != null ? String.valueOf(analysis.getTotalWorkoutSessions()) : "0";
        String mealCount = analysis.getTotalMeals() != null ? String.valueOf(analysis.getTotalMeals()) : "0";
        String mealSummary = formatMealSummary(analysis.getMeals());
        String workoutSummary = formatWorkoutSummary(analysis.getWorkouts());

        return """
                You are an AI fitness coach writing personalized daily coaching for a mobile app.
                Generate the advice text only. Do not return JSON, markdown, headings, or bullet points.
                Use only the structured data below. If a value is missing, say so briefly and do not invent details.

                User daily summary:
                Date: %s
                Calories consumed: %s
                Calories burned: %s
                Calorie balance: %s
                Steps: %s
                Meals logged: %s
                Workout sessions logged: %s

                Meals:
                %s

                Workout:
                %s

                Goal:
                Goal type: %s
                Target weight: %s
                Target calories: %s
                Target protein: %s
                Target carbs: %s
                Target fat: %s

                Body metrics:
                Weight: %s
                Sex: %s
                Activity level: %s

                Advice requirements:
                - Write 3 to 4 sentences maximum.
                - First sentence must summarize the main fitness pattern from the daily log.
                - Mention calorie balance, activity level, and goal alignment when relevant.
                - Use the meal and workout summary to comment on nutrition quality or training consistency only when the data supports it.
                - Give concrete next-step recommendations for the next day.
                - Do not hallucinate food details, workout details, or targets that are not provided.
                """.formatted(
                analysis.getDate() != null ? analysis.getDate() : "unknown",
                caloriesConsumed,
                caloriesBurned,
                calorieBalance,
                steps,
                mealCount,
                workoutCount,
                mealSummary,
                workoutSummary,
                goalType,
                targetWeight,
                targetCalories,
                targetProtein,
                targetCarbs,
                targetFat,
                latestWeight,
                sex,
                activityLevel
        );
    }

    public String buildChatPrompt(ChatPromptContext context, String conversationHistory, String latestUserMessage) {
        return """
                You are a continuous AI fitness coach in an ongoing conversation.

                Current user context:
                - goalType: %s
                - targetCalories: %s
                - targetProtein: %s
                - targetCarbs: %s
                - targetFat: %s
                - caloriesConsumed: %s
                - caloriesBurned: %s
                - calorieBalance: %s
                - steps: %s
                - latestWeight: %s

                Conversation rules:
                - Be supportive, clear, and actionable.
                - Keep replies short: at most 4 sentences.
                - Avoid repetition.
                - Use the recent conversation and the latest fitness context.
                - If data is missing, say so briefly and still help.

                Recent conversation:
                %s

                Latest user message:
                %s
                """.formatted(
                context.goalType(),
                context.targetCalories(),
                context.targetProtein(),
                context.targetCarbs(),
                context.targetFat(),
                context.caloriesConsumed(),
                context.caloriesBurned(),
                context.calorieBalance(),
                context.steps(),
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
            double caloriesConsumed,
            double caloriesBurned,
            double calorieBalance,
            int steps,
            Object latestWeight
    ) {
    }

    private String formatMealSummary(List<AIMealSummaryResponse> meals) {
        if (meals == null || meals.isEmpty()) {
            return "- No meals logged";
        }

        return meals.stream()
                .map(meal -> "- " + titleCase(meal.getMealType())
                        + ": " + valueOrUnknown(meal.getTotalItems())
                        + " item(s), " + formatBigDecimal(meal.getTotalCalories()) + " kcal")
                .collect(Collectors.joining(System.lineSeparator()));
    }

    private String formatWorkoutSummary(List<AIWorkoutSummaryResponse> workouts) {
        if (workouts == null || workouts.isEmpty()) {
            return "- No workout sessions logged";
        }

        return workouts.stream()
                .map(workout -> "- " + valueOrUnknown(workout.getExerciseName())
                        + ", duration " + valueOrUnknown(workout.getDuration()) + " min"
                        + ", calories " + formatBigDecimal(workout.getCaloriesBurned()) + " kcal")
                .collect(Collectors.joining(System.lineSeparator()));
    }

    private String formatBigDecimal(BigDecimal value) {
        return value != null ? value.stripTrailingZeros().toPlainString() : "0";
    }

    private String formatNullableNumber(Number value) {
        return value != null ? String.valueOf(value) : "unknown";
    }

    private String valueOrUnknown(Object value) {
        return value != null ? String.valueOf(value) : "unknown";
    }

    private String titleCase(String value) {
        if (value == null || value.isBlank()) {
            return "Unknown";
        }

        return java.util.Arrays.stream(value.toLowerCase(Locale.ROOT).split("_|\\s+"))
                .filter(part -> !part.isBlank())
                .map(part -> Character.toUpperCase(part.charAt(0)) + part.substring(1))
                .collect(Collectors.joining(" "));
=======
        String prompt = """
                You are a professional fitness coach.

                User data:
                %s

                Give short, actionable advice in 2-3 sentences.
                """.formatted(analysis);

        return prompt;
>>>>>>> main
    }
}
