package com.fitness.fitnessaicoach.service;

import com.fitness.fitnessaicoach.dto.ai.AIAnalysisResponse;
import org.springframework.stereotype.Component;

@Component
public class PromptBuilder {

    public String buildPrompt(AIAnalysisResponse analysis) {
        String goalType = analysis.getGoalType() != null ? analysis.getGoalType().name() : "UNKNOWN";
        String targetCalories = analysis.getTargetCalories() != null ? String.valueOf(analysis.getTargetCalories()) : "unknown";
        String targetProtein = analysis.getTargetProtein() != null ? String.valueOf(analysis.getTargetProtein()) : "unknown";
        String targetCarbs = analysis.getTargetCarbs() != null ? String.valueOf(analysis.getTargetCarbs()) : "unknown";
        String targetFat = analysis.getTargetFat() != null ? String.valueOf(analysis.getTargetFat()) : "unknown";
        String caloriesConsumed = analysis.getTotalCaloriesConsumed() != null ? analysis.getTotalCaloriesConsumed().toPlainString() : "0";
        String caloriesBurned = analysis.getTotalCaloriesBurned() != null ? analysis.getTotalCaloriesBurned().toPlainString() : "0";
        String calorieBalance = analysis.getCalorieBalance() != null ? analysis.getCalorieBalance().toPlainString() : "0";
        String steps = analysis.getTotalSteps() != null ? String.valueOf(analysis.getTotalSteps()) : "0";
        String latestWeight = analysis.getLatestWeight() != null ? String.valueOf(analysis.getLatestWeight()) : "unknown";
        String workoutsPerformed = analysis.getTotalWorkoutSessions() != null
                ? String.valueOf(analysis.getTotalWorkoutSessions())
                : "0";

        return """
                You are an AI fitness coach writing personalized daily coaching for a mobile app.

                The backend already returns the final API shape as:
                {
                  "analysis": string,
                  "advice": string
                }

                Your job is to generate the advice content only. Do not return JSON, markdown, bullets, or labels.

                Structured input:
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
                - workoutsPerformed: %s

                Coaching priorities, in this order:
                1. Adherence to the calorie target.
                2. Activity level versus the 7000 steps baseline.
                3. Consistency with the current goalType.
                4. Weight trend direction using the latest available weight.

                Goal-specific guidance:
                - LOSE_WEIGHT: favor a sustainable calorie deficit, more walking when steps are low, and lower-calorie food choices.
                - BUILD_MUSCLE: support adequate protein, resistance training, and a controlled calorie surplus.
                - MAINTAIN: support stable intake, stable activity, and avoidance of large calorie swings.

                Tone requirements:
                - supportive
                - clear
                - actionable
                - specific, not generic
                - avoid repetition and vague motivational filler

                Output requirements:
                - Write 3 to 4 sentences maximum.
                - First sentence should objectively explain the most important pattern in the data.
                - Remaining sentence(s) should give concrete next actions for the next day.
                - Mention calorie target adherence, steps, and goal alignment when relevant.
                - If some data are missing, say so briefly and still give the best next action.

                Full analysis object for reference:
                %s
                """.formatted(
                goalType,
                targetCalories,
                targetProtein,
                targetCarbs,
                targetFat,
                caloriesConsumed,
                caloriesBurned,
                calorieBalance,
                steps,
                latestWeight,
                workoutsPerformed,
                analysis
        );
    }
}
