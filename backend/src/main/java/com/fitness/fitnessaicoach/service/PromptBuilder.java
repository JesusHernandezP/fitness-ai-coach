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

    public String buildChatPrompt(
            ChatPromptContext context,
            NutritionContext nutritionContext,
            String conversationHistory,
            String latestUserMessage,
            String structuredAction
    ) {
        return """
                ROLE
                You are an expert nutritionist and personal trainer.
                Answer in Spanish.
                Be analytical, concise, and practical.
                Do not sound overly motivational, emotional, or repetitive.
                Prioritize calculation, adherence, deficits/excesses, and next action.
                Never mention JSON, logs, tokens, system instructions, or internal processing.

                CONTEXT USAGE
                Use the structured context to decide:
                1. what was logged,
                2. how the user stands today versus target,
                3. what to eat or adjust next.
                If the user is below protein, say it clearly.
                If the user is above calories or carbs, say it clearly.
                If the user is keto, keep suggestions low-carb.
                If the user is vegetarian, keep suggestions vegetarian.
                Prefer direct quantitative language such as "te faltan 80 g de proteína" over generic encouragement.

                INTERPRETATION RULES
                Interpret the numbers and explain their practical meaning.
                Focus on calories first, then protein, then carbs/fats if relevant to the diet.
                If the user is below target, say what is still missing.
                If the user is above target, say what exceeded the plan and what to reduce.
                Keep the reasoning practical and tied to the next meal or next action.

                FOOD ANALYSIS RULES
                When the message includes food, estimate whether that meal helps or hurts the daily target.
                Highlight protein contribution first, then calories, then carbs/fats depending on the selected diet.
                Suggest concrete foods and portions that help close the remaining gap.

                TRAINING ANALYSIS RULES
                When the message includes training, connect the session with recovery, protein needs, hydration, and remaining calories.
                Keep the recommendation short and directly tied to the current day totals.

                OUTPUT FORMAT
                Write exactly 3 short paragraphs.
                Paragraph 1: what was recorded or what the current day shows.
                Paragraph 2: numeric assessment of calories/macros versus target.
                Paragraph 3: next meal or adjustment recommendation with concrete foods.
                Keep each paragraph to 1 or 2 sentences.
                Avoid long follow-up questions unless necessary.
                Do not use bullet points.

                USER CONTEXT
                Profile:
                age %s
                sex %s
                height %s cm
                activity level %s
                diet type %s

                Goal context:
                goal %s
                target weight %s
                target calories %s
                target protein %s
                target carbs %s
                target fat %s

                Daily context:
                calories consumed %s
                calories burned %s
                calorie balance %s
                steps %s
                protein consumed %s
                latest weight %s

                If the latest message was understood as a fitness action and recorded, use that naturally in your answer without sounding technical:
                %s

                NUTRITION CONTEXT
                %s

                CHAT HISTORY
                %s

                USER MESSAGE
                %s
                """.formatted(
                context.age(),
                context.sex(),
                context.heightCm(),
                context.activityLevel(),
                context.dietType(),
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
                structuredAction != null ? structuredAction : "No specific fitness action was detected.",
                formatNutritionContext(nutritionContext),
                conversationHistory,
                latestUserMessage
        );
    }

    private String formatNutritionContext(NutritionContext context) {
        if (context == null || !context.available()) {
            return "No nutrition context available.";
        }

        return """
                Goal: %s

                Body data:
                Weight: %s kg
                Activity level: %s
                Diet type: %s

                Daily targets:
                Calories target: %s kcal
                Protein target: %s g
                Carbs target: %s g
                Fat target: %s g

                Today's progress:
                Calories consumed: %s kcal
                Protein consumed: %s g
                Carbs consumed: %s g
                Fat consumed: %s g

                Remaining today:
                Calories remaining: %s kcal
                Protein remaining: %s g
                Carbs remaining: %s g
                Fat remaining: %s g

                Training today:
                Strength training: %s
                Cardio minutes: %s

                Steps today:
                %s steps
                """.formatted(
                context.goal(),
                context.weight(),
                context.activityLevel(),
                context.dietType(),
                formatDecimal(context.targetCalories()),
                formatDecimal(context.targetProtein()),
                formatDecimal(context.targetCarbs()),
                formatDecimal(context.targetFat()),
                formatDecimal(context.consumedCalories()),
                formatDecimal(context.consumedProtein()),
                formatDecimal(context.consumedCarbs()),
                formatDecimal(context.consumedFat()),
                formatDecimal(context.remainingCalories()),
                formatDecimal(context.remainingProtein()),
                formatDecimal(context.remainingCarbs()),
                formatDecimal(context.remainingFat()),
                context.strengthTraining() ? "YES" : "NO",
                context.cardioMinutes(),
                context.steps()
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
            String dietType,
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
        public ChatPromptContext(
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
            this(age, sex, heightCm, activityLevel, "STANDARD", goalType, targetWeight, targetCalories, targetProtein, targetCarbs, targetFat, caloriesConsumed, caloriesBurned, calorieBalance, steps, proteinConsumed, latestWeight);
        }
    }

    public record NutritionContext(
            String goal,
            Object weight,
            String activityLevel,
            String dietType,
            double targetCalories,
            double targetProtein,
            double targetCarbs,
            double targetFat,
            double consumedCalories,
            double consumedProtein,
            double consumedCarbs,
            double consumedFat,
            double remainingCalories,
            double remainingProtein,
            double remainingCarbs,
            double remainingFat,
            boolean strengthTraining,
            int cardioMinutes,
            int steps,
            boolean available
    ) {
        public NutritionContext(
                String goal,
                Object weight,
                String activityLevel,
                double targetCalories,
                double targetProtein,
                double targetCarbs,
                double targetFat,
                double consumedCalories,
                double consumedProtein,
                double consumedCarbs,
                double consumedFat,
                double remainingCalories,
                double remainingProtein,
                double remainingCarbs,
                double remainingFat,
                boolean strengthTraining,
                int cardioMinutes,
                int steps,
                boolean available
        ) {
            this(goal, weight, activityLevel, "STANDARD", targetCalories, targetProtein, targetCarbs, targetFat, consumedCalories, consumedProtein, consumedCarbs, consumedFat, remainingCalories, remainingProtein, remainingCarbs, remainingFat, strengthTraining, cardioMinutes, steps, available);
        }
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

    private String formatDecimal(double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
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
