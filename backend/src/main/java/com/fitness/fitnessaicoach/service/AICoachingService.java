package com.fitness.fitnessaicoach.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.fitnessaicoach.ai.provider.AITextGenerationClient;
import com.fitness.fitnessaicoach.domain.AIRecommendation;
import com.fitness.fitnessaicoach.domain.BodyMetrics;
import com.fitness.fitnessaicoach.domain.DailyLog;
import com.fitness.fitnessaicoach.domain.Goal;
import com.fitness.fitnessaicoach.domain.User;
import com.fitness.fitnessaicoach.dto.ai.AICoachingResponse;
import com.fitness.fitnessaicoach.dto.ai.AIAnalysisResponse;
import com.fitness.fitnessaicoach.dto.ai.AIWeeklySummaryResponse;
import com.fitness.fitnessaicoach.exception.UserNotFoundException;
import com.fitness.fitnessaicoach.repository.AIRecommendationRepository;
import com.fitness.fitnessaicoach.repository.BodyMetricsRepository;
import com.fitness.fitnessaicoach.repository.DailyLogRepository;
import com.fitness.fitnessaicoach.repository.GoalRepository;
import com.fitness.fitnessaicoach.repository.UserRepository;
import com.fitness.fitnessaicoach.repository.WorkoutSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
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
    private final UserRepository userRepository;
    private final DailyLogRepository dailyLogRepository;
    private final BodyMetricsRepository bodyMetricsRepository;
    private final GoalRepository goalRepository;
    private final WorkoutSessionRepository workoutSessionRepository;

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

    @Transactional(readOnly = true)
    public AIWeeklySummaryResponse getWeeklySummary(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found."));

        LocalDate referenceDate = dailyLogRepository.findTopByUserIdOrderByLogDateDescIdDesc(user.getId())
                .map(DailyLog::getLogDate)
                .orElse(LocalDate.now());
        LocalDate weekStart = referenceDate.minusDays(6);
        LocalDate weekEnd = referenceDate;

        List<DailyLog> weeklyLogs = dailyLogRepository.findByUserIdAndLogDateBetweenOrderByLogDateAsc(
                user.getId(),
                weekStart,
                weekEnd
        );

        if (weeklyLogs.isEmpty()) {
            return new AIWeeklySummaryResponse(
                    weekStart,
                    weekEnd,
                    "No weekly fitness data has been logged yet.",
                    "Start by logging meals, steps, workouts, and weight consistently during the week."
            );
        }

        Goal latestGoal = goalRepository.findTopByUserIdOrderByCreatedAtDescIdDesc(user.getId()).orElse(null);
        List<BodyMetrics> weeklyMetrics = bodyMetricsRepository.findByUserIdOrderByDateAsc(user.getId()).stream()
                .filter(metric -> !metric.getDate().isBefore(weekStart) && !metric.getDate().isAfter(weekEnd))
                .toList();
        BodyMetrics latestMetric = bodyMetricsRepository.findTopByUserIdOrderByDateDescIdDesc(user.getId()).orElse(null);

        int averageSteps = (int) Math.round(weeklyLogs.stream()
                .mapToInt(log -> log.getSteps() != null ? log.getSteps() : 0)
                .average()
                .orElse(0));
        double averageCaloriesConsumed = weeklyLogs.stream()
                .mapToDouble(log -> log.getCaloriesConsumed() != null ? log.getCaloriesConsumed() : 0.0)
                .average()
                .orElse(0.0);
        double averageCaloriesBurned = weeklyLogs.stream()
                .mapToDouble(log -> log.getCaloriesBurned() != null ? log.getCaloriesBurned() : 0.0)
                .average()
                .orElse(0.0);
        int totalWorkouts = weeklyLogs.stream()
                .mapToInt(log -> Math.toIntExact(workoutSessionRepository.countByDailyLogId(log.getId())))
                .sum();

        String startingWeight = weeklyMetrics.stream()
                .min(Comparator.comparing(BodyMetrics::getDate))
                .map(metric -> formatDecimal(metric.getWeight()))
                .orElse("unknown");
        String endingWeight = weeklyMetrics.stream()
                .max(Comparator.comparing(BodyMetrics::getDate))
                .map(metric -> formatDecimal(metric.getWeight()))
                .orElse("unknown");
        String weightTrend = resolveWeightTrend(weeklyMetrics);

        PromptBuilder.WeeklySummaryPromptContext promptContext = new PromptBuilder.WeeklySummaryPromptContext(
                weekStart,
                weekEnd,
                weeklyLogs.size(),
                formatDecimal(averageCaloriesConsumed),
                formatDecimal(averageCaloriesBurned),
                averageSteps,
                totalWorkouts,
                latestGoal != null ? latestGoal.getGoalType().name() : "UNKNOWN",
                latestMetric != null ? formatDecimal(latestMetric.getWeight()) : "unknown",
                startingWeight,
                endingWeight,
                weightTrend
        );

        try {
            String rawResponse = aiTextGenerationClient.generateText(promptBuilder.buildWeeklySummaryPrompt(promptContext));
            WeeklySummaryContent content = objectMapper.readValue(rawResponse, WeeklySummaryContent.class);
            if (content.summary == null || content.summary.isBlank()
                    || content.recommendation == null || content.recommendation.isBlank()) {
                throw new IllegalStateException("Incomplete weekly summary response.");
            }
            return new AIWeeklySummaryResponse(weekStart, weekEnd, content.summary.trim(), content.recommendation.trim());
        } catch (Exception exception) {
            log.warn("Falling back to deterministic weekly summary: {}", exception.getMessage());
            return buildFallbackWeeklySummary(weekStart, weekEnd, latestGoal, averageCaloriesConsumed, averageCaloriesBurned, averageSteps, totalWorkouts, weightTrend);
        }
    }

    private AIWeeklySummaryResponse buildFallbackWeeklySummary(
            LocalDate weekStart,
            LocalDate weekEnd,
            Goal latestGoal,
            double averageCaloriesConsumed,
            double averageCaloriesBurned,
            int averageSteps,
            int totalWorkouts,
            String weightTrend
    ) {
        String goalLabel = latestGoal != null ? latestGoal.getGoalType().name().toLowerCase().replace('_', ' ') : "general fitness";
        String balanceDirection = averageCaloriesConsumed <= averageCaloriesBurned ? "calorie deficit" : "calorie surplus";

        String summary = "You maintained an average of %s calories consumed, %s calories burned, and %s daily steps across the week. "
                .formatted(formatDecimal(averageCaloriesConsumed), formatDecimal(averageCaloriesBurned), averageSteps)
                + "Your activity shows %s workout session(s) with a %s trend that is %s."
                .formatted(totalWorkouts, weightTrend, goalLabel);

        String recommendation = "Keep aiming for %s while improving consistency in steps and workouts. "
                .formatted(goalLabel)
                + "Based on your current %s, target at least %s steps per day and maintain regular training."
                .formatted(balanceDirection, Math.max(averageSteps, 8000));

        return new AIWeeklySummaryResponse(weekStart, weekEnd, summary, recommendation);
    }

    private String resolveWeightTrend(List<BodyMetrics> weeklyMetrics) {
        if (weeklyMetrics.size() < 2) {
            return "stable";
        }

        double firstWeight = weeklyMetrics.get(0).getWeight();
        double lastWeight = weeklyMetrics.get(weeklyMetrics.size() - 1).getWeight();
        double difference = lastWeight - firstWeight;

        if (difference > 0.2) {
            return "increasing";
        }
        if (difference < -0.2) {
            return "decreasing";
        }
        return "stable";
    }

    private String formatDecimal(double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }

    private static class WeeklySummaryContent {
        public String summary;
        public String recommendation;
    }
}
