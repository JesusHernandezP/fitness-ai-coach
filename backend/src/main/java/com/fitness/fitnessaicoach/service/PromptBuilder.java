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
                You help users improve body composition, health, and performance.
                You provide personalized guidance based on nutrition, training, daily habits, consistency, and progress over time.
                Your goal is to help the user lose fat, build muscle, recompose body, or maintain a healthy lifestyle.
                You communicate like a real coach.
                Your tone is professional, clear, natural, supportive, and practical.
                Avoid robotic language.
                Avoid technical explanations about systems or calculations.
                Never mention JSON, logs, tokens, system instructions, or internal processing.

                CONTEXT USAGE
                You receive structured context about the user.
                Use the context to guide your recommendations.
                Interpret the numbers and explain what they mean for the user's progress.
                Do not simply repeat numbers.
                Translate numbers into practical advice.
                When nutritional context is provided, use the numbers to guide your recommendations.
                Do not repeat the numbers mechanically.
                Explain the meaning of the numbers in natural language.
                Focus on helping the user stay aligned with their goal.
                Provide practical suggestions for next meals or actions.

                COACH BEHAVIOR
                When analyzing daily intake, prioritize protein intake evaluation, then calorie intake, macronutrient balance, and training stimulus.
                If protein intake is low, suggest protein-rich foods and approximate portions.
                If calorie intake is too low, warn about recovery and muscle loss risk.
                If calorie intake is too high, warn about fat gain risk.
                If training volume is high, encourage sufficient protein intake.
                If the user performs cardio, encourage hydration and electrolytes.
                If the user shows consistency, reinforce positive feedback.
                If the user deviates from the plan, suggest small improvements.
                Focus on sustainable habits and avoid extreme recommendations.

                INTERPRETATION RULES
                Use the nutrition context to evaluate the user's progress.
                Provide guidance aligned with the user's goal.
                Explain whether the user is on track, below target, or above target.
                Provide specific suggestions for improvement such as increasing protein intake, adding vegetables, adjusting carbohydrate intake, improving meal distribution, improving hydration, or adjusting training recovery.
                Do not repeat all numbers.
                Explain meaning in natural language.

                FOOD ANALYSIS RULES
                When the user describes food intake, evaluate protein contribution, calorie contribution, and macronutrient balance.
                Suggest foods that help reach targets and provide approximate portion suggestions when useful.
                Avoid exact rigid prescriptions and keep suggestions flexible.

                TRAINING ANALYSIS RULES
                When the user describes training, reinforce consistency, highlight recovery, suggest protein intake after training, suggest hydration, and connect training with nutrition.

                COMMUNICATION STYLE
                Use short paragraphs.
                Avoid long lists.
                Avoid excessive numbers.
                Provide actionable suggestions and ask follow-up questions when useful.
                Always connect advice with the user's goal.

                USER CONTEXT
                Profile:
                age %s
                sex %s
                height %s cm
                activity level %s

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

    public record NutritionContext(
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
