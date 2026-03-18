package com.fitness.fitnessaicoach.service;

import com.fitness.fitnessaicoach.ai.provider.groq.GroqClient;
import com.fitness.fitnessaicoach.dto.ai.AICoachingAdviceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AICoachingService {

    private final AIAnalysisService aiAnalysisService;
    private final PromptBuilder promptBuilder;
    private final GroqClient groqClient;

    public AICoachingAdviceResponse generateCoachingAdvice(UUID dailyLogId) {
        var analysis = aiAnalysisService.getDailyLogAiAnalysis(dailyLogId);
        String prompt = promptBuilder.buildPrompt(analysis);
        String advice = groqClient.getCoachingResponse(prompt);

        return new AICoachingAdviceResponse(dailyLogId, advice);
    }
}
