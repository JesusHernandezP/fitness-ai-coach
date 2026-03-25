package com.fitness.fitnessaicoach;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.fitnessaicoach.ai.provider.groq.GroqClient;
import com.fitness.fitnessaicoach.config.GroqConfig;
import com.fitness.fitnessaicoach.domain.AIRecommendation;
import com.fitness.fitnessaicoach.dto.ai.AIAnalysisResponse;
import com.fitness.fitnessaicoach.dto.ai.AIMealSummaryResponse;
import com.fitness.fitnessaicoach.dto.ai.AIWorkoutSummaryResponse;
import com.fitness.fitnessaicoach.dto.ai.AICoachingResponse;
import com.fitness.fitnessaicoach.repository.AIRecommendationRepository;
import com.fitness.fitnessaicoach.service.AICoachingService;
import com.fitness.fitnessaicoach.service.AIAnalysisService;
import com.fitness.fitnessaicoach.service.PromptBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AICoachingServiceTest {

    @Mock
    private AIAnalysisService aiAnalysisService;

    @Mock
    private PromptBuilder promptBuilder;

    @Mock
    private GroqClient groqClient;

    @Mock
    private AIRecommendationRepository aiRecommendationRepository;

    @Mock
    private GroqConfig groqConfig;

    private AICoachingService aiCoachingService;

    @BeforeEach
    void setUp() {
        aiCoachingService = new AICoachingService(
                aiAnalysisService,
                promptBuilder,
                groqClient,
                aiRecommendationRepository,
                new ObjectMapper(),
                groqConfig
        );
    }

    @Test
    void generateCoachingAdviceBuildsPromptAndReturnsGroqAdvice() {
        UUID dailyLogId = UUID.randomUUID();
        AIAnalysisResponse analysis = AIAnalysisResponse.builder()
                .dailyLogId(dailyLogId)
                .totalMeals(1)
                .totalWorkoutSessions(0)
                .meals(List.of(AIMealSummaryResponse.builder().mealType("BREAKFAST").totalItems(1).totalCalories(BigDecimal.TEN).build()))
                .workouts(List.of(AIWorkoutSummaryResponse.builder().exerciseName("Push Up").duration(20).caloriesBurned(BigDecimal.valueOf(120)).build()))
                .build();

        String builtPrompt = "prompt-text";
        when(aiAnalysisService.getDailyLogAiAnalysis(dailyLogId)).thenReturn(analysis);
        when(promptBuilder.buildPrompt(analysis)).thenReturn(builtPrompt);
        when(groqClient.getCoachingResponse("prompt-text")).thenReturn("Advice from Groq");
        when(groqConfig.getModel()).thenReturn("llama-test");

        AICoachingResponse response = aiCoachingService.getCoaching(dailyLogId);

        ArgumentCaptor<UUID> idCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(aiAnalysisService).getDailyLogAiAnalysis(idCaptor.capture());
        verify(promptBuilder).buildPrompt(analysis);
        verify(groqClient).getCoachingResponse("prompt-text");
        verify(aiRecommendationRepository).save(any(AIRecommendation.class));

        assertThat(idCaptor.getValue()).isEqualTo(dailyLogId);
        assertThat(response.getAnalysis().getDailyLogId()).isEqualTo(dailyLogId);
        assertThat(response.getAdvice()).isEqualTo("Advice from Groq");
    }

    @Test
    void getCoachingReturnsFallbackAdviceOnGroqFailure() {
        UUID dailyLogId = UUID.randomUUID();
        AIAnalysisResponse analysis = AIAnalysisResponse.builder()
                .dailyLogId(dailyLogId)
                .totalMeals(0)
                .totalWorkoutSessions(0)
                .build();

        String builtPrompt = "prompt-text";
        when(aiAnalysisService.getDailyLogAiAnalysis(dailyLogId)).thenReturn(analysis);
        when(promptBuilder.buildPrompt(analysis)).thenReturn(builtPrompt);
        when(groqClient.getCoachingResponse("prompt-text")).thenThrow(new IllegalStateException("groq timeout"));
        when(groqConfig.getModel()).thenReturn("llama-test");

        AICoachingResponse response = aiCoachingService.getCoaching(dailyLogId);

        assertThat(response.getAdvice())
                .isEqualTo("AI coaching is temporarily unavailable. Please review your daily log summary and try again later.");
        verify(aiRecommendationRepository).save(any(AIRecommendation.class));
    }
}
