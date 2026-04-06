package com.fitness.fitnessaicoach.service;

import com.fitness.fitnessaicoach.domain.AIChatMessage;
import com.fitness.fitnessaicoach.domain.ChatRole;
import com.fitness.fitnessaicoach.domain.ChatSession;
import com.fitness.fitnessaicoach.domain.User;
import com.fitness.fitnessaicoach.dto.ai.AIChatMessageDto;
import com.fitness.fitnessaicoach.exception.UserNotFoundException;
import com.fitness.fitnessaicoach.repository.AIChatMessageRepository;
import com.fitness.fitnessaicoach.repository.ChatSessionRepository;
import com.fitness.fitnessaicoach.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AIChatHistoryService {

    private static final int MAX_STORED_MESSAGES = 20;

    private final ChatSessionRepository chatSessionRepository;
    private final AIChatMessageRepository aiChatMessageRepository;
    private final UserRepository userRepository;

    @Transactional
    public ChatSession saveUserMessage(UUID userId, String message) {
        User user = findUser(userId);
        ChatSession session = getOrCreateActiveSession(user);
        saveMessage(session, ChatRole.USER, message);
        return session;
    }

    @Transactional
    public void saveAssistantMessage(UUID userId, String message) {
        User user = findUser(userId);
        ChatSession session = getOrCreateActiveSession(user);
        saveMessage(session, ChatRole.AI, message);
        trimStoredMessages(session.getId());
    }

    @Transactional(readOnly = true)
    public List<AIChatMessageDto> getChatHistory(UUID userId) {
        findUser(userId);
        return aiChatMessageRepository.findBySessionUserIdOrderByCreatedAtAscIdAsc(userId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AIChatMessage> getRecentMessages(UUID userId, int limit) {
        List<AIChatMessage> recentMessages = aiChatMessageRepository.findTop20BySessionUserIdOrderByCreatedAtDescIdDesc(userId);
        if (recentMessages.size() <= limit) {
            return recentMessages;
        }
        return recentMessages.subList(0, limit);
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found."));
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

    private void trimStoredMessages(UUID sessionId) {
        List<AIChatMessage> allMessages = aiChatMessageRepository.findBySessionIdOrderByCreatedAtAscIdAsc(sessionId);
        int messagesToDelete = allMessages.size() - MAX_STORED_MESSAGES;
        if (messagesToDelete <= 0) {
            return;
        }

        aiChatMessageRepository.deleteAll(allMessages.subList(0, messagesToDelete));
    }

    private AIChatMessageDto toDto(AIChatMessage message) {
        String role = message.getRole() == ChatRole.USER ? "USER" : "ASSISTANT";
        return new AIChatMessageDto(role, message.getContent(), message.getCreatedAt());
    }
}
