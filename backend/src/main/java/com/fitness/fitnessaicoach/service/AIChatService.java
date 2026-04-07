package com.fitness.fitnessaicoach.service;

import com.fitness.fitnessaicoach.ai.provider.AITextGenerationClient;
import com.fitness.fitnessaicoach.domain.AIChatMessage;
import com.fitness.fitnessaicoach.domain.ChatRole;
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

import java.util.Collections;
import java.util.Comparator;
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
            reply = buildFallbackReply(user, dailySummary, processing.loggedSummary());
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
        List<AIChatMessage> recentMessages = aiChatHistoryService.getRecentMessages(userId, 8);
        Collections.reverse(recentMessages);
        return promptBuilder.buildChatPrompt(
                context,
                nutritionContext,
                formatConversation(excludeLatestUserTurn(recentMessages, latestUserMessage)),
                latestUserMessage,
                structuredAction
        );
    }

    private String buildFallbackReply(User user, DailyNutritionSummaryResponse dailySummary, List<String> loggedSummary) {
        String note = dailySummary.getAdherenceNotes() != null && !dailySummary.getAdherenceNotes().isEmpty()
                ? dailySummary.getAdherenceNotes().get(0)
                : "Sigue registrando tu dia para afinar las recomendaciones.";
        if ((user.getDietType() != null ? user.getDietType() : DietType.STANDARD) == DietType.KETO
                && dailySummary.getRemainingProtein() != null
                && dailySummary.getRemainingFat() != null) {
            return "Buen registro. " + note + " Aun te faltan " + round(dailySummary.getRemainingProtein())
                    + " g de proteina y " + round(dailySummary.getRemainingFat())
                    + " g de grasa; para keto puedes completar con pechuga de pollo, atun, huevos o aguacate.";
        }
        return "Buen registro. " + note + " Hoy te faltan " + round(dailySummary.getRemainingProtein())
                + " g de proteina y " + round(dailySummary.getRemainingCalories())
                + " kcal para cerrar mejor el dia.";
    }

    private String round(Double value) {
        if (value == null) {
            return "0";
        }
        return value == Math.rint(value) ? String.valueOf(value.longValue()) : String.format(java.util.Locale.US, "%.1f", value);
    }

    private String formatConversation(List<AIChatMessage> recentMessages) {
        if (recentMessages.isEmpty()) {
            return "No previous messages.";
        }

        return recentMessages.stream()
                .sorted(Comparator.comparing(AIChatMessage::getCreatedAt).thenComparing(AIChatMessage::getId))
                .map(message -> message.getRole().name() + ": " + message.getContent())
                .reduce((left, right) -> left + System.lineSeparator() + right)
                .orElse("No previous messages.");
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
}
