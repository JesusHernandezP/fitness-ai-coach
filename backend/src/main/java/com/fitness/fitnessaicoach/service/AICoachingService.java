package com.fitness.fitnessaicoach.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.fitnessaicoach.ai.provider.AITextGenerationClient;
import com.fitness.fitnessaicoach.domain.AIRecommendation;
import com.fitness.fitnessaicoach.dto.ai.AICoachingResponse;
import com.fitness.fitnessaicoach.dto.ai.AICoachingAdviceResponse;
import com.fitness.fitnessaicoach.dto.ai.AIAnalysisResponse;
import com.fitness.fitnessaicoach.repository.AIRecommendationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AICoachingService {

    private final AIAnalysisService aiAnalysisService;
    private final PromptBuilder promptBuilder;
    private final AITextGenerationClient aiTextGenerationClient;
    private final AIRecommendationRepository aiRecommendationRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public AICoachingResponse getCoaching(UUID dailyLogId) {
        AIRecommendation storedRecommendation = aiRecommendationRepository
                .findFirstByDailyLogIdOrderByCreatedAtDescIdDesc(dailyLogId)
                .orElse(null);
        if (storedRecommendation != null) {
            log.info("Returning stored AI coaching for dailyLogId={}", dailyLogId);
            return new AICoachingResponse(
                    storedRecommendation.getAnalysisSnapshot(),
                    storedRecommendation.getAdvice()
            );
        }

        log.info("Generating AI coaching for dailyLogId={}", dailyLogId);

        AIAnalysisResponse analysis = aiAnalysisService.getDailyLogAiAnalysis(dailyLogId);
        String analysisSnapshot = toJson(analysis);
        String prompt = promptBuilder.buildPrompt(analysis);
        String advice;

        try {
            advice = aiTextGenerationClient.generateText(prompt);
        } catch (Exception e) {
            log.error("AI error", e);
            advice = fallbackAdvice();
        }

        saveRecommendation(dailyLogId, analysisSnapshot, advice);

        return new AICoachingResponse(analysisSnapshot, advice);
    }

    private String fallbackAdvice() {
        return "AI coaching is temporarily unavailable. Please review your daily log summary and try again later.";
    }

    private void saveRecommendation(UUID dailyLogId, String analysisSnapshot, String advice) {
        AIRecommendation recommendation = AIRecommendation.builder()
                .dailyLogId(dailyLogId)
                .analysisSnapshot(analysisSnapshot)
                .advice(advice)
                .model(aiTextGenerationClient.getModelName())
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
        return new AICoachingAdviceResponse(dailyLogId, response.getAdvice());
    }
}
