package com.fitness.fitnessaicoach.service;

import com.fitness.fitnessaicoach.ai.provider.AITextGenerationClient;
import com.fitness.fitnessaicoach.domain.AIChatMessage;
import com.fitness.fitnessaicoach.domain.ChatRole;
import com.fitness.fitnessaicoach.domain.ChatSession;
import com.fitness.fitnessaicoach.domain.User;
import com.fitness.fitnessaicoach.dto.ai.AIChatMessageResponse;
import com.fitness.fitnessaicoach.exception.UserNotFoundException;
import com.fitness.fitnessaicoach.repository.AIChatMessageRepository;
import com.fitness.fitnessaicoach.repository.ChatSessionRepository;
import com.fitness.fitnessaicoach.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AIChatService {

    private static final int MAX_STORED_MESSAGES = 20;

    private final ChatSessionRepository chatSessionRepository;
    private final AIChatMessageRepository aiChatMessageRepository;
    private final UserRepository userRepository;
    private final AIIntentService aiIntentService;
    private final AITextGenerationClient aiTextGenerationClient;
    private final PromptBuilder promptBuilder;

    @Transactional
    public AIChatMessageResponse sendMessage(String email, String messageContent) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found."));

        ChatSession session = getOrCreateActiveSession(user);
        saveMessage(session, ChatRole.USER, messageContent);

        String reply = aiIntentService.handleIntent(user, messageContent)
                .orElseGet(() -> aiTextGenerationClient.generateText(buildPrompt(user.getId(), messageContent)).trim());

        saveMessage(session, ChatRole.AI, reply);
        session.setLastActivityAt(LocalDateTime.now());
        chatSessionRepository.save(session);
        trimStoredMessages(session.getId());

        return new AIChatMessageResponse(reply);
    }

    private ChatSession getOrCreateActiveSession(User user) {
        return chatSessionRepository.findTopByUserIdOrderByLastActivityAtDescIdDesc(user.getId())
                .map(existing -> {
                    existing.setLastActivityAt(LocalDateTime.now());
                    return chatSessionRepository.save(existing);
                })
                .orElseGet(() -> chatSessionRepository.save(ChatSession.builder()
                        .user(user)
                        .lastActivityAt(LocalDateTime.now())
                        .build()));
    }

    private void saveMessage(ChatSession session, ChatRole role, String content) {
        aiChatMessageRepository.save(AIChatMessage.builder()
                .session(session)
                .role(role)
                .content(content)
                .build());
    }

    private String buildPrompt(UUID userId, String latestUserMessage) {
        PromptBuilder.ChatPromptContext context = aiIntentService.buildPromptContext(userId);
        List<AIChatMessage> recentMessages = aiChatMessageRepository.findTop20BySessionUserIdOrderByCreatedAtDescIdDesc(userId);
        Collections.reverse(recentMessages);
        return promptBuilder.buildChatPrompt(context, formatConversation(excludeLatestUserTurn(recentMessages, latestUserMessage)), latestUserMessage);
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

    private void trimStoredMessages(UUID sessionId) {
        List<AIChatMessage> allMessages = aiChatMessageRepository.findBySessionIdOrderByCreatedAtAscIdAsc(sessionId);
        int messagesToDelete = allMessages.size() - MAX_STORED_MESSAGES;
        if (messagesToDelete <= 0) {
            return;
        }

        aiChatMessageRepository.deleteAll(allMessages.subList(0, messagesToDelete));
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
