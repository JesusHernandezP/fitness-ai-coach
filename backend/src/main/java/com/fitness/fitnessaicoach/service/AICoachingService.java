package com.fitness.fitnessaicoach.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
<<<<<<< HEAD
import com.fitness.fitnessaicoach.ai.provider.AITextGenerationClient;
=======
import com.fitness.fitnessaicoach.ai.provider.groq.GroqClient;
import com.fitness.fitnessaicoach.config.GroqConfig;
>>>>>>> main
import com.fitness.fitnessaicoach.domain.AIRecommendation;
import com.fitness.fitnessaicoach.dto.ai.AICoachingResponse;
import com.fitness.fitnessaicoach.dto.ai.AICoachingAdviceResponse;
import com.fitness.fitnessaicoach.dto.ai.AIAnalysisResponse;
import com.fitness.fitnessaicoach.repository.AIRecommendationRepository;
<<<<<<< HEAD
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
=======
import com.fitness.fitnessaicoach.security.LogSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
>>>>>>> main

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AICoachingService {

    private final AIAnalysisService aiAnalysisService;
    private final PromptBuilder promptBuilder;
<<<<<<< HEAD
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
=======
    private final GroqClient groqClient;
    private final AIRecommendationRepository aiRecommendationRepository;
    private final ObjectMapper objectMapper;
    private final GroqConfig groqConfig;

    public AICoachingResponse getCoaching(UUID dailyLogId) {
        log.info("Generating AI coaching for dailyLogId={}", dailyLogId);

        AIAnalysisResponse analysis = aiAnalysisService.getDailyLogAiAnalysis(dailyLogId);
>>>>>>> main
        String prompt = promptBuilder.buildPrompt(analysis);
        String advice;

        try {
<<<<<<< HEAD
            advice = aiTextGenerationClient.generateText(prompt);
        } catch (Exception e) {
            log.error("AI error", e);
            advice = fallbackAdvice();
        }

        saveRecommendation(dailyLogId, analysisSnapshot, advice);

        return new AICoachingResponse(analysisSnapshot, advice);
=======
            advice = groqClient.getCoachingResponse(prompt);
        } catch (Exception e) {
            log.error(
                    "AI coaching failed for dailyLogId={} reason={}",
                    dailyLogId,
                    LogSanitizer.sanitizeExceptionMessage(e)
            );
            log.debug("AI coaching stacktrace for dailyLogId={}", dailyLogId, e);
            advice = fallbackAdvice();
        }

        saveRecommendation(dailyLogId, analysis, advice);

        return new AICoachingResponse(analysis, advice);
>>>>>>> main
    }

    private String fallbackAdvice() {
        return "AI coaching is temporarily unavailable. Please review your daily log summary and try again later.";
    }

<<<<<<< HEAD
    private void saveRecommendation(UUID dailyLogId, String analysisSnapshot, String advice) {
        AIRecommendation recommendation = AIRecommendation.builder()
                .dailyLogId(dailyLogId)
                .analysisSnapshot(analysisSnapshot)
                .advice(advice)
                .model(aiTextGenerationClient.getModelName())
=======
    private void saveRecommendation(UUID dailyLogId, AIAnalysisResponse analysis, String advice) {
        AIRecommendation recommendation = AIRecommendation.builder()
                .dailyLogId(dailyLogId)
                .analysisSnapshot(toJson(analysis))
                .advice(advice)
                .model(groqConfig.getModel())
>>>>>>> main
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
<<<<<<< HEAD
        return new AICoachingAdviceResponse(dailyLogId, response.getAdvice());
=======
        return new AICoachingAdviceResponse(response.getAnalysis().getDailyLogId(), response.getAdvice());
>>>>>>> main
    }
}
