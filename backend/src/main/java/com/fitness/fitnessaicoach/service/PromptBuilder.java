package com.fitness.fitnessaicoach.service;

import com.fitness.fitnessaicoach.dto.ai.AIAnalysisResponse;
import org.springframework.stereotype.Component;

@Component
public class PromptBuilder {

    public String buildPrompt(AIAnalysisResponse analysis) {
        String prompt = """
                You are a professional fitness coach.

                User data:
                %s

                Give short, actionable advice in 2-3 sentences.
                """.formatted(analysis);

        return prompt;
    }
}
