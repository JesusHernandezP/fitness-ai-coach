package com.fitness.fitnessaicoach.service;

import com.fitness.fitnessaicoach.ai.provider.groq.GroqClient;
import com.fitness.fitnessaicoach.dto.ai.AICoachingResponse;
import com.fitness.fitnessaicoach.dto.ai.AICoachingAdviceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AICoachingService {

    private final AIAnalysisService aiAnalysisService;
    private final PromptBuilder promptBuilder;
    private final GroqClient groqClient;

    public AICoachingResponse getCoaching(UUID dailyLogId) {
        log.info("Generating AI coaching for dailyLogId={}", dailyLogId);

        var analysis = aiAnalysisService.getDailyLogAiAnalysis(dailyLogId);
        String prompt = promptBuilder.buildPrompt(analysis);
        String advice;

        try {
            advice = groqClient.getCoachingResponse(prompt);
        } catch (Exception e) {
            log.error("AI error", e);
            advice = fallbackAdvice();
        }

        return new AICoachingResponse(analysis, advice);
    }

    private String fallbackAdvice() {
        return "AI coaching is temporarily unavailable. Please review your daily log summary and try again later.";
    }

    @Deprecated
    public AICoachingAdviceResponse generateCoachingAdvice(UUID dailyLogId) {
        AICoachingResponse response = getCoaching(dailyLogId);
        return new AICoachingAdviceResponse(response.getAnalysis().getDailyLogId(), response.getAdvice());
    }
}
