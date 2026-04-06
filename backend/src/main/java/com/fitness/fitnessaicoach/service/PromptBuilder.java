package com.fitness.fitnessaicoach.service;

import com.fitness.fitnessaicoach.dto.ai.AIAnalysisResponse;
import com.fitness.fitnessaicoach.dto.ai.AIMealSummaryResponse;
import com.fitness.fitnessaicoach.dto.ai.AIWorkoutSummaryResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
public class PromptBuilder {

    public String buildPrompt(AIAnalysisResponse analysis) {
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
                You are a conversational fitness coach for a mobile app.
                Behave like a nutritionist and personal trainer.
                Your tone must be concise, supportive, and professional.

                User profile:
                - age: %s
                - sex: %s
                - heightCm: %s
                - activityLevel: %s

                Goals:
                - goalType: %s
                - targetWeight: %s
                - targetCalories: %s
                - targetProtein: %s
                - targetCarbs: %s
                - targetFat: %s

                Current daily summary:
                - caloriesConsumed: %s
                - caloriesBurned: %s
                - calorieBalance: %s
                - steps: %s
                - proteinConsumed: %s
                - latestWeight: %s

                Conversation rules:
                - Keep replies short: 2 to 4 sentences.
                - Use recent conversation and stored fitness data as context.
                - If the user asks for advice, reference goal alignment, activity, nutrition, or recovery when relevant.
                - If data is missing, say so briefly and still give a practical next step.
                - Never invent values that are not present.
                - Do not return JSON, markdown tables, or bullet lists.

                Recent conversation:
                %s

                Latest user message:
                %s
                """.formatted(
                context.age(),
                context.sex(),
                context.heightCm(),
                context.activityLevel(),
                context.goalType(),
                context.targetWeight(),
                context.targetCalories(),
                context.targetProtein(),
                context.targetCarbs(),
                context.targetFat(),
                context.caloriesConsumed(),
                context.caloriesBurned(),
                context.calorieBalance(),
                context.steps(),
                context.proteinConsumed(),
                context.latestWeight(),
                conversationHistory,
                latestUserMessage
        );
    }

    public String buildWeeklySummaryPrompt(WeeklySummaryPromptContext context) {
        return """
                You are an AI fitness coach generating a weekly progress summary for a dashboard.
                Respond with strict JSON only using this exact shape:
                {"summary":"...","recommendation":"..."}

                Use only the data provided below. Do not invent values.
                Write both fields as short plain text sentences.
                The summary should describe trend and adherence.
                The recommendation should suggest one or two next steps.

                Weekly context:
                Week start: %s
                Week end: %s
                Days logged: %s
                Average calories consumed: %s
                Average calories burned: %s
                Average steps: %s
                Total workouts: %s
                Goal type: %s
                Latest weight: %s
                Starting weight: %s
                Ending weight: %s
                Weight trend: %s
                """.formatted(
                context.weekStart(),
                context.weekEnd(),
                context.daysLogged(),
                context.averageCaloriesConsumed(),
                context.averageCaloriesBurned(),
                context.averageSteps(),
                context.totalWorkouts(),
                context.goalType(),
                context.latestWeight(),
                context.startingWeight(),
                context.endingWeight(),
                context.weightTrend()
        );
    }

    public record ChatPromptContext(
            Object age,
            String sex,
            Object heightCm,
            String activityLevel,
            String goalType,
            Object targetWeight,
            Object targetCalories,
            Object targetProtein,
            Object targetCarbs,
            Object targetFat,
            double caloriesConsumed,
            double caloriesBurned,
            double calorieBalance,
            int steps,
            double proteinConsumed,
            Object latestWeight
    ) {
    }

    public record WeeklySummaryPromptContext(
            LocalDate weekStart,
            LocalDate weekEnd,
            int daysLogged,
            String averageCaloriesConsumed,
            String averageCaloriesBurned,
            int averageSteps,
            int totalWorkouts,
            String goalType,
            String latestWeight,
            String startingWeight,
            String endingWeight,
            String weightTrend
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
    }
}
