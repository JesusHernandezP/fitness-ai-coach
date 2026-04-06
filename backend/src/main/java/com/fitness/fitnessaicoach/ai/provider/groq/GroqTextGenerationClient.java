package com.fitness.fitnessaicoach.ai.provider.groq;

import com.fitness.fitnessaicoach.ai.provider.AITextGenerationClient;
import com.fitness.fitnessaicoach.config.GroqConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GroqTextGenerationClient implements AITextGenerationClient {

    private final GroqClient groqClient;
    private final GroqConfig groqConfig;

    @Override
    public String generateText(String prompt) {
        return groqClient.getCoachingResponse(prompt);
    }

    @Override
    public String getModelName() {
        return groqConfig.getModel();
    }
}
