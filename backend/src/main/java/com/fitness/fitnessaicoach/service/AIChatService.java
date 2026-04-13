package com.fitness.fitnessaicoach.service;

import com.fitness.fitnessaicoach.ai.provider.AITextGenerationClient;
import com.fitness.fitnessaicoach.domain.ActivityLevel;
import com.fitness.fitnessaicoach.domain.AIChatMessage;
import com.fitness.fitnessaicoach.domain.BodyMetrics;
import com.fitness.fitnessaicoach.domain.ChatRole;
import com.fitness.fitnessaicoach.domain.ChatSession;
import com.fitness.fitnessaicoach.domain.DailyLog;
import com.fitness.fitnessaicoach.domain.Goal;
import com.fitness.fitnessaicoach.domain.User;
import com.fitness.fitnessaicoach.domain.UserGoalType;
import com.fitness.fitnessaicoach.domain.UserSex;
import com.fitness.fitnessaicoach.exception.UserNotFoundException;
import com.fitness.fitnessaicoach.repository.AIChatMessageRepository;
import com.fitness.fitnessaicoach.repository.BodyMetricsRepository;
import com.fitness.fitnessaicoach.repository.ChatSessionRepository;
import com.fitness.fitnessaicoach.repository.DailyLogRepository;
import com.fitness.fitnessaicoach.repository.GoalRepository;
import com.fitness.fitnessaicoach.repository.UserRepository;
import com.fitness.fitnessaicoach.security.LogSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIChatService {

    private static final int MAX_STORED_MESSAGES = 20;
    private static final int MAX_CONTEXT_MESSAGES = 8;

    private final ChatSessionRepository chatSessionRepository;
    private final AIChatMessageRepository aiChatMessageRepository;
    private final UserRepository userRepository;
    private final GoalRepository goalRepository;
    private final BodyMetricsRepository bodyMetricsRepository;
    private final DailyLogRepository dailyLogRepository;
    private final AIIntentService aiIntentService;
    private final AITextGenerationClient aiTextGenerationClient;

    @Transactional
    public String sendMessage(String email, String messageContent, UUID conversationId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found."));

        ChatSession session = resolveSession(user, conversationId);
        saveMessage(session, ChatRole.USER, messageContent);

        String reply = aiIntentService.handleIntent(user, messageContent)
                .orElseGet(() -> generateCoachReply(user, session, messageContent));

        saveMessage(session, ChatRole.AI, reply);
        session.setLastActivityAt(LocalDateTime.now());
        chatSessionRepository.save(session);
        trimStoredMessages(session.getId());

        return reply;
    }

    private ChatSession resolveSession(User user, UUID conversationId) {
        if (conversationId != null) {
            return chatSessionRepository.findByIdAndUserId(conversationId, user.getId())
                    .orElseGet(() -> createNewSession(user));
        }

        return chatSessionRepository.findTopByUserIdOrderByLastActivityAtDescIdDesc(user.getId())
                .map(existing -> {
                    existing.setLastActivityAt(LocalDateTime.now());
                    return chatSessionRepository.save(existing);
                })
                .orElseGet(() -> createNewSession(user));
    }

    private ChatSession createNewSession(User user) {
        return chatSessionRepository.save(ChatSession.builder()
                .user(user)
                .lastActivityAt(LocalDateTime.now())
                .build());
    }

    private void saveMessage(ChatSession session, ChatRole role, String content) {
        aiChatMessageRepository.save(AIChatMessage.builder()
                .session(session)
                .role(role)
                .content(content)
                .build());
    }

    private String generateCoachReply(User user, ChatSession session, String latestUserMessage) {
        try {
            UserContext context = loadUserContext(user);
            String prompt = buildPrompt(context, session.getId(), latestUserMessage);
            String response = aiTextGenerationClient.generateText(prompt);
            return response != null ? response.trim() : "";
        } catch (Exception exception) {
            log.error(
                    "AI chat failed for userId={} reason={}",
                    user.getId(),
                    LogSanitizer.sanitizeExceptionMessage(exception)
            );
            log.debug("AI chat stacktrace for userId={}", user.getId(), exception);
            return "El chat con el coach esta temporalmente no disponible. Puedes seguir registrando comidas, entrenamientos, pasos y peso mientras se recupera.";
        }
    }

    private UserContext loadUserContext(User user) {
        Goal latestGoal = goalRepository.findTopByUserIdOrderByCreatedAtDescIdDesc(user.getId()).orElse(null);
        BodyMetrics latestMetrics = bodyMetricsRepository.findTopByUserIdOrderByDateDescIdDesc(user.getId()).orElse(null);
        DailyLog todayLog = dailyLogRepository.findByUserIdAndLogDate(user.getId(), LocalDate.now()).orElse(null);

        return new UserContext(
                user.getAge(),
                user.getSex(),
                user.getHeightCm(),
                user.getWeightKg(),
                latestMetrics != null ? latestMetrics.getBodyFat() : null,
                user.getActivityLevel(),
                latestGoal != null ? latestGoal.getGoalType() : null,
                latestGoal != null ? latestGoal.getTargetWeight() : null,
                todayLog,
                null,
                null,
                null,
                null,
                null
        );
    }

    private String buildPrompt(UserContext context, UUID sessionId, String latestUserMessage) {
        String systemPrompt = buildSystemPrompt();
        String contextSummary = buildContextSummary(context);
        String conversation = buildConversationHistory(sessionId, latestUserMessage);

        StringBuilder prompt = new StringBuilder();
        prompt.append("SYSTEM:\n").append(systemPrompt).append("\n\n");
        if (!contextSummary.isBlank()) {
            prompt.append("CONTEXT SUMMARY:\n").append(contextSummary).append("\n\n");
        }
        if (!conversation.isBlank()) {
            prompt.append("RECENT CONVERSATION:\n").append(conversation).append("\n\n");
        }
        prompt.append("CURRENT USER MESSAGE:\n").append(latestUserMessage).append("\n\n");
        prompt.append("ASSISTANT RESPONSE:");
        return prompt.toString();
    }

    private String buildSystemPrompt() {
        return """
                Eres un coach de fitness y nutricion con enfoque practico.
                Responde de forma natural, conversacional y en espanol.

                Ayudas a las personas a mejorar su composicion corporal, salud y habitos.
                Usa datos del usuario cuando existan: edad, sexo, altura, peso, grasa corporal, nivel de actividad, objetivos, registros diarios y metas caloricas.

                Cuando haya datos suficientes: estima necesidades caloricas, estima proteina, evalua la ingesta diaria y sugiere mejoras.
                Cuando falten datos: haz preguntas de seguimiento utiles sin repetir lo mismo.
                Si falta informacion puntual, asume de forma razonable y explica con tacto.

                Evita valores exactos si no son razonables; usa rangos cuando hables de calorias o macros.
                Evita relleno generico y manten respuestas practicas y personalizadas.
                Manten continuidad conversacional.
                No menciones sistemas internos ni bases de datos.

                Puedes generar planes semanales, sugerencias de comidas, ajustes diarios, evaluacion de progreso, estimaciones caloricas y recomendaciones de habitos.
                No menciones formulas salvo que sea util.
                Manten explicaciones concisas.
                """;
    }

    private String buildContextSummary(UserContext context) {
        StringBuilder summary = new StringBuilder();

        if (context.age() != null) {
            summary.append("Edad: ").append(context.age()).append("\n");
        }
        if (context.sex() != null) {
            summary.append("Sexo: ").append(context.sex().name()).append("\n");
        }
        if (context.heightCm() != null) {
            summary.append("Altura: ").append(formatNumber(context.heightCm())).append(" cm\n");
        }
        if (context.weightKg() != null) {
            summary.append("Peso: ").append(formatNumber(context.weightKg())).append(" kg\n");
        }
        if (context.bodyFat() != null) {
            summary.append("Grasa corporal: ").append(formatNumber(context.bodyFat())).append("%\n");
        }
        if (context.activityLevel() != null) {
            summary.append("Nivel de actividad: ").append(context.activityLevel().name().toLowerCase(Locale.ROOT)).append("\n");
        }
        if (context.goalType() != null) {
            summary.append("Objetivo: ").append(formatGoal(context.goalType())).append("\n");
        }
        if (context.targetWeight() != null) {
            summary.append("Peso objetivo: ").append(formatNumber(context.targetWeight())).append(" kg\n");
        }

        if (context.todayLog() != null) {
            summary.append("Registro de hoy: pasos=").append(valueOrDefault(context.todayLog().getSteps(), 0))
                    .append(", calorias consumidas=").append(valueOrDefault(context.todayLog().getCaloriesConsumed(), 0.0))
                    .append(", calorias quemadas=").append(valueOrDefault(context.todayLog().getCaloriesBurned(), 0.0))
                    .append("\n");
        }

        DerivedMetrics metrics = computeDerivedMetrics(context);
        if (metrics != null) {
            summary.append("TDEE estimado: ").append(formatNumber(metrics.tdee())).append(" kcal\n");
            if (metrics.recommendedCalories() != null) {
                summary.append("Calorias recomendadas: ").append(formatNumber(metrics.recommendedCalories())).append(" kcal\n");
            }
            if (metrics.recommendedProteinGrams() != null) {
                summary.append("Proteina recomendada: ").append(formatNumber(metrics.recommendedProteinGrams())).append(" g\n");
            }
        }

        return summary.toString().trim();
    }

    private String buildConversationHistory(UUID sessionId, String latestUserMessage) {
        List<AIChatMessage> recentMessages = aiChatMessageRepository.findTop8BySessionIdOrderByCreatedAtDescIdDesc(sessionId);
        if (recentMessages.isEmpty()) {
            return "";
        }

        Collections.reverse(recentMessages);
        List<AIChatMessage> sanitized = excludeLatestUserTurn(recentMessages, latestUserMessage);

        return sanitized.stream()
                .sorted(Comparator.comparing(AIChatMessage::getCreatedAt).thenComparing(AIChatMessage::getId))
                .map(message -> formatRole(message.getRole()) + ": " + message.getContent())
                .reduce((left, right) -> left + System.lineSeparator() + right)
                .orElse("");
    }

    private List<AIChatMessage> excludeLatestUserTurn(List<AIChatMessage> recentMessages, String latestUserMessage) {
        if (recentMessages.isEmpty()) {
            return recentMessages;
        }

        AIChatMessage lastMessage = recentMessages.get(recentMessages.size() - 1);
        if (lastMessage.getRole() == ChatRole.USER && latestUserMessage.equals(lastMessage.getContent())) {
            return recentMessages.subList(0, recentMessages.size() - 1);
        }

        return recentMessages;
    }

    private DerivedMetrics computeDerivedMetrics(UserContext context) {
        if (context.age() == null || context.heightCm() == null || context.weightKg() == null || context.sex() == null) {
            return null;
        }

        double bmr = calculateBmr(context.weightKg(), context.heightCm(), context.age(), context.sex());
        double tdee = bmr * activityMultiplier(context.activityLevel());
        Double recommendedCalories = calculateRecommendedCalories(tdee, context.goalType());
        Double recommendedProtein = calculateRecommendedProtein(context);

        return new DerivedMetrics(roundTwoDecimals(tdee), recommendedCalories, recommendedProtein);
    }

    private double calculateBmr(double weightKg, double heightCm, int age, UserSex sex) {
        double base = (10 * weightKg) + (6.25 * heightCm) - (5 * age);
        return base + (sex == UserSex.MALE ? 5.0 : -161.0);
    }

    private double activityMultiplier(ActivityLevel activityLevel) {
        if (activityLevel == null) {
            return 1.2;
        }
        return switch (activityLevel) {
            case SEDENTARY -> 1.2;
            case LIGHT -> 1.375;
            case MODERATE -> 1.55;
            case ACTIVE -> 1.725;
            case VERY_ACTIVE -> 1.9;
        };
    }

    private Double calculateRecommendedCalories(double tdee, UserGoalType goalType) {
        if (goalType == null) {
            return roundTwoDecimals(tdee);
        }
        double adjusted = switch (goalType) {
            case LOSE_WEIGHT -> tdee - 300;
            case BUILD_MUSCLE -> tdee + 300;
            case MAINTAIN -> tdee;
        };
        return roundTwoDecimals(adjusted);
    }

    private Double calculateRecommendedProtein(UserContext context) {
        Double baseWeight = context.targetWeight() != null ? context.targetWeight() : context.weightKg();
        if (baseWeight == null) {
            return null;
        }

        double multiplier = switch (Optional.ofNullable(context.goalType()).orElse(UserGoalType.MAINTAIN)) {
            case LOSE_WEIGHT -> 2.0;
            case BUILD_MUSCLE -> 2.2;
            case MAINTAIN -> 1.6;
        };

        return roundTwoDecimals(baseWeight * multiplier);
    }

    private void trimStoredMessages(UUID sessionId) {
        List<AIChatMessage> allMessages = aiChatMessageRepository.findBySessionIdOrderByCreatedAtAscIdAsc(sessionId);
        int messagesToDelete = allMessages.size() - MAX_STORED_MESSAGES;
        if (messagesToDelete <= 0) {
            return;
        }

        aiChatMessageRepository.deleteAll(allMessages.subList(0, messagesToDelete));
    }

    private String formatRole(ChatRole role) {
        return role == ChatRole.USER ? "USER" : "ASSISTANT";
    }

    private String formatGoal(UserGoalType goalType) {
        return switch (goalType) {
            case LOSE_WEIGHT -> "perder peso";
            case BUILD_MUSCLE -> "ganar musculo";
            case MAINTAIN -> "mantener";
        };
    }

    private String formatNumber(Double value) {
        if (value == null) {
            return "desconocido";
        }
        if (value == Math.rint(value)) {
            return String.valueOf(value.longValue());
        }
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private double roundTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private int valueOrDefault(Integer value, int fallback) {
        return value != null ? value : fallback;
    }

    private double valueOrDefault(Double value, double fallback) {
        return value != null ? value : fallback;
    }

    private record UserContext(
            Integer age,
            UserSex sex,
            Double heightCm,
            Double weightKg,
            Double bodyFat,
            ActivityLevel activityLevel,
            UserGoalType goalType,
            Double targetWeight,
            DailyLog todayLog,
            String dietPreference,
            Integer trainingDays,
            Integer trainingDuration,
            String trainingLocation,
            String injuries
    ) {
    }

    private record DerivedMetrics(
            double tdee,
            Double recommendedCalories,
            Double recommendedProteinGrams
    ) {
    }
}
