package com.fitness.fitnessaicoach.service;

import com.fitness.fitnessaicoach.ai.provider.AITextGenerationClient;
import com.fitness.fitnessaicoach.domain.DietType;
import com.fitness.fitnessaicoach.domain.User;
import com.fitness.fitnessaicoach.dto.DailyNutritionSummaryResponse;
import com.fitness.fitnessaicoach.dto.ai.AIChatMessageDto;
import com.fitness.fitnessaicoach.dto.ai.AIChatMessageResponse;
import com.fitness.fitnessaicoach.exception.UserNotFoundException;
import com.fitness.fitnessaicoach.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AIChatService {

    private final UserRepository userRepository;
    private final AIChatHistoryService aiChatHistoryService;
    private final AIIntentService aiIntentService;
    private final NutritionContextBuilder nutritionContextBuilder;
    private final NutritionSummaryService nutritionSummaryService;
    private final AITextGenerationClient aiTextGenerationClient;
    private final PromptBuilder promptBuilder;

    @Transactional
    public AIChatMessageResponse sendMessage(String email, String messageContent) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found."));

        aiChatHistoryService.saveUserMessage(user.getId(), messageContent);
        AIIntentService.ChatIntentResult processing = aiIntentService.processMessage(user, messageContent);
        DailyNutritionSummaryResponse dailySummary = nutritionSummaryService.buildForDate(user.getId(), processing.targetDate());
        String reply;
        try {
            reply = aiTextGenerationClient.generateText(buildPrompt(user.getId(), messageContent, processing.structuredAction())).trim();
        } catch (Exception exception) {
            reply = "";
        }
        if (reply.isBlank()) {
            reply = buildDeterministicReply(user, dailySummary, processing.loggedSummary());
        }

        aiChatHistoryService.saveAssistantMessage(user.getId(), reply);

        return new AIChatMessageResponse(reply, processing.loggedSummary(), dailySummary);
    }

    @Transactional(readOnly = true)
    public List<AIChatMessageDto> getChatHistory(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found."));
        return aiChatHistoryService.getChatHistory(user.getId());
    }

    private String buildPrompt(UUID userId, String latestUserMessage, String structuredAction) {
        PromptBuilder.ChatPromptContext context = aiIntentService.buildPromptContext(userId);
        PromptBuilder.NutritionContext nutritionContext = nutritionContextBuilder.build(userId);
        List<com.fitness.fitnessaicoach.domain.AIChatMessage> recentMessages = aiChatHistoryService.getRecentMessages(userId, 8);
        java.util.Collections.reverse(recentMessages);
        return promptBuilder.buildChatPrompt(
                context,
                nutritionContext,
                formatConversation(excludeLatestUserTurn(recentMessages, latestUserMessage)),
                latestUserMessage,
                structuredAction
        );
    }

    private String buildDeterministicReply(User user, DailyNutritionSummaryResponse dailySummary, List<String> loggedSummary) {
        StringBuilder builder = new StringBuilder();

        if (loggedSummary != null && !loggedSummary.isEmpty()) {
            builder.append("Registrado hoy: ")
                    .append(String.join("; ", loggedSummary))
                    .append(".");
        } else {
            builder.append("No registré nuevos datos en este mensaje.");
        }

        builder.append(System.lineSeparator()).append(System.lineSeparator())
                .append("Acumulado del día: ")
                .append(round(dailySummary.getConsumedCalories())).append("/")
                .append(round(dailySummary.getTargetCalories())).append(" kcal, ")
                .append(round(dailySummary.getConsumedProtein())).append("/")
                .append(round(dailySummary.getTargetProtein())).append(" g proteína, ")
                .append(round(dailySummary.getConsumedCarbs())).append("/")
                .append(round(dailySummary.getTargetCarbs())).append(" g carbohidratos, ")
                .append(round(dailySummary.getConsumedFat())).append("/")
                .append(round(dailySummary.getTargetFat())).append(" g grasa.");

        builder.append(System.lineSeparator())
                .append("Actividad: ")
                .append(valueOrZero(dailySummary.getSteps())).append(" pasos y ")
                .append(round(dailySummary.getCaloriesBurned())).append(" kcal quemadas.");

        builder.append(System.lineSeparator()).append(System.lineSeparator())
                .append(buildAssessment(dailySummary));

        String dietAdvice = buildDietAdvice(user, dailySummary);
        if (!dietAdvice.isBlank()) {
            builder.append(System.lineSeparator()).append(dietAdvice);
        }

        return builder.toString().trim();
    }

    private String buildAssessment(DailyNutritionSummaryResponse dailySummary) {
        double remainingCalories = safe(dailySummary.getRemainingCalories());
        double remainingProtein = safe(dailySummary.getRemainingProtein());
        double remainingCarbs = safe(dailySummary.getRemainingCarbs());
        double remainingFat = safe(dailySummary.getRemainingFat());
        double consumedCalories = safe(dailySummary.getConsumedCalories());
        double targetCalories = safe(dailySummary.getTargetCalories());
        double targetProtein = safe(dailySummary.getTargetProtein());
        double consumedProtein = safe(dailySummary.getConsumedProtein());

        if (targetCalories > 0 && consumedCalories > targetCalories) {
            return "Balance: vas " + round(consumedCalories - targetCalories)
                    + " kcal por encima del objetivo. Recorta densidad calórica en la siguiente comida y prioriza proteína magra y verduras.";
        }

        if (targetProtein > 0 && consumedProtein < targetProtein * 0.7) {
            return "Proteína: vas corto para el punto del día. Te faltan " + round(remainingProtein)
                    + " g y conviene cerrarlos antes de aumentar calorías vacías.";
        }

        return "Restante: " + round(remainingCalories) + " kcal, "
                + round(remainingProtein) + " g proteína, "
                + round(remainingCarbs) + " g carbohidratos y "
                + round(remainingFat) + " g grasa.";
    }

    private String buildDietAdvice(User user, DailyNutritionSummaryResponse dailySummary) {
        DietType dietType = user.getDietType() != null ? user.getDietType() : DietType.STANDARD;
        double remainingProtein = safe(dailySummary.getRemainingProtein());
        double remainingCarbs = safe(dailySummary.getRemainingCarbs());
        double remainingFat = safe(dailySummary.getRemainingFat());
        double consumedCarbs = safe(dailySummary.getConsumedCarbs());
        double targetCarbs = safe(dailySummary.getTargetCarbs());

        if (dietType == DietType.KETO) {
            String base = "Keto: completa lo que falta con pollo, atún, huevos, salmón, aguacate y aceite de oliva.";
            if (targetCarbs > 0 && consumedCarbs > targetCarbs) {
                return "Keto: ya superaste el rango de carbohidratos del día. Evita arroz, pan, fruta dulce y azúcares; céntrate en proteína y grasa.";
            }
            return base + " Te faltan " + round(remainingProtein) + " g proteína y "
                    + round(remainingFat) + " g grasa; no conviene subir mucho más los carbohidratos.";
        }

        if (dietType == DietType.VEGETARIAN) {
            return "Vegetariana: cierra el día con yogur griego, huevos, tofu, tempeh, queso fresco o legumbres. Te faltan "
                    + round(remainingProtein) + " g proteína y " + round(remainingCarbs) + " g carbohidratos.";
        }

        return "Siguiente ajuste: prioriza proteína para cerrar el día. Te faltan "
                + round(remainingProtein) + " g proteína; puedes usar pechuga de pollo, atún, claras, yogur griego o carne magra.";
    }

    private double safe(Double value) {
        return value != null ? value : 0.0;
    }

    private String valueOrZero(Integer value) {
        return value != null ? String.valueOf(value) : "0";
    }

    private String round(Double value) {
        if (value == null) {
            return "0";
        }
        return value == Math.rint(value) ? String.valueOf(value.longValue()) : String.format(java.util.Locale.US, "%.1f", value);
    }

    private String formatConversation(List<com.fitness.fitnessaicoach.domain.AIChatMessage> recentMessages) {
        if (recentMessages.isEmpty()) {
            return "No previous messages.";
        }

        return recentMessages.stream()
                .sorted(java.util.Comparator.comparing(com.fitness.fitnessaicoach.domain.AIChatMessage::getCreatedAt)
                        .thenComparing(com.fitness.fitnessaicoach.domain.AIChatMessage::getId))
                .map(message -> message.getRole().name() + ": " + message.getContent())
                .reduce((left, right) -> left + System.lineSeparator() + right)
                .orElse("No previous messages.");
    }

    private List<com.fitness.fitnessaicoach.domain.AIChatMessage> excludeLatestUserTurn(
            List<com.fitness.fitnessaicoach.domain.AIChatMessage> recentMessages,
            String latestUserMessage
    ) {
        if (recentMessages.isEmpty()) {
            return recentMessages;
        }

        com.fitness.fitnessaicoach.domain.AIChatMessage lastMessage = recentMessages.get(recentMessages.size() - 1);
        if (lastMessage.getRole() == com.fitness.fitnessaicoach.domain.ChatRole.USER
                && latestUserMessage.equals(lastMessage.getContent())) {
            return recentMessages.subList(0, recentMessages.size() - 1);
        }

        return recentMessages;
    }
}
