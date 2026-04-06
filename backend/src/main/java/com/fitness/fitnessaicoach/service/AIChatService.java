package com.fitness.fitnessaicoach.service;

import com.fitness.fitnessaicoach.ai.provider.AITextGenerationClient;
import com.fitness.fitnessaicoach.domain.AIChatMessage;
import com.fitness.fitnessaicoach.domain.ChatRole;
import com.fitness.fitnessaicoach.domain.User;
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
    private final AITextGenerationClient aiTextGenerationClient;
    private final PromptBuilder promptBuilder;

    @Transactional
    public AIChatMessageResponse sendMessage(String email, String messageContent) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found."));

        aiChatHistoryService.saveUserMessage(user.getId(), messageContent);
        String structuredAction = aiIntentService.handleIntent(user, messageContent).orElse(null);
        String reply = aiTextGenerationClient.generateText(buildPrompt(user.getId(), messageContent, structuredAction)).trim();
        if (reply.isBlank() && structuredAction != null) {
            reply = structuredAction;
        }

        aiChatHistoryService.saveAssistantMessage(user.getId(), reply);

        return new AIChatMessageResponse(reply);
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
        List<AIChatMessage> recentMessages = aiChatHistoryService.getRecentMessages(userId, 20);
        Collections.reverse(recentMessages);
        return promptBuilder.buildChatPrompt(
                context,
                nutritionContext,
                formatConversation(excludeLatestUserTurn(recentMessages, latestUserMessage)),
                latestUserMessage,
                structuredAction
        );
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
