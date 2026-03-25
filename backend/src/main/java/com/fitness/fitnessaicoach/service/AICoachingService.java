package com.fitness.fitnessaicoach.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.fitnessaicoach.ai.provider.groq.GroqClient;
import com.fitness.fitnessaicoach.config.GroqConfig;
import com.fitness.fitnessaicoach.domain.AIRecommendation;
import com.fitness.fitnessaicoach.dto.ai.AICoachingResponse;
import com.fitness.fitnessaicoach.dto.ai.AICoachingAdviceResponse;
import com.fitness.fitnessaicoach.dto.ai.AIAnalysisResponse;
import com.fitness.fitnessaicoach.repository.AIRecommendationRepository;
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
    private final AIRecommendationRepository aiRecommendationRepository;
    private final ObjectMapper objectMapper;
    private final GroqConfig groqConfig;

    public AICoachingResponse getCoaching(UUID dailyLogId) {
        log.info("Generating AI coaching for dailyLogId={}", dailyLogId);

        AIAnalysisResponse analysis = aiAnalysisService.getDailyLogAiAnalysis(dailyLogId);
        String prompt = promptBuilder.buildPrompt(analysis);
        String advice;

        try {
            advice = groqClient.getCoachingResponse(prompt);
        } catch (Exception e) {
            log.error("AI error", e);
            advice = fallbackAdvice();
        }

        saveRecommendation(dailyLogId, analysis, advice);

        return new AICoachingResponse(analysis, advice);
    }

    private String fallbackAdvice() {
        return "AI coaching is temporarily unavailable. Please review your daily log summary and try again later.";
    }

    private void saveRecommendation(UUID dailyLogId, AIAnalysisResponse analysis, String advice) {
        AIRecommendation recommendation = AIRecommendation.builder()
                .dailyLogId(dailyLogId)
                .analysisSnapshot(toJson(analysis))
                .advice(advice)
                .model(groqConfig.getModel())
                .build();

        aiRecommendationRepository.save(recommendation);
    }

    private String toJson(AIAnalysisResponse analysis) {
        try {
            return objectMapper.writeValueAsString(analysis);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize AI analysis snapshot.", e);
        }
    }

    @Deprecated
    public AICoachingAdviceResponse generateCoachingAdvice(UUID dailyLogId) {
        AICoachingResponse response = getCoaching(dailyLogId);
        return new AICoachingAdviceResponse(response.getAnalysis().getDailyLogId(), response.getAdvice());
    }
}
