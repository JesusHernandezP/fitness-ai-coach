package com.fitness.fitnessaicoach.service;

import com.fitness.fitnessaicoach.dto.ai.AIAnalysisResponse;
import org.springframework.stereotype.Component;

@Component
public class PromptBuilder {

    public String buildPrompt(AIAnalysisResponse analysis) {
        return """
                You are a professional fitness coach.

                Daily log analysis:
                %s

                Evaluate:
                - calorie balance versus targetCalories
                - steps versus the 7000 minimum
                - whether workouts were performed
                - the user's goalType context

                Behavior rules:
                - If goalType is LOSE_WEIGHT, recommend a calorie deficit, encourage more steps when below target, and suggest lower calorie foods.
                - If goalType is BUILD_MUSCLE, encourage protein intake, resistance training, and allow a slight calorie surplus.
                - If goalType is MAINTAIN, encourage balanced intake and avoiding large calorie deviations.

                Return short, actionable advice in 2-3 sentences.
                """.formatted(analysis);
    }
}
