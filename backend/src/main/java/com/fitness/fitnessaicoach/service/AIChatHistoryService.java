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
import java.time.LocalDate;
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
        User user = findUser(userId);
        return findCurrentDailySession(user)
                .map(session -> aiChatMessageRepository.findBySessionIdOrderByCreatedAtAscIdAsc(session.getId()).stream()
                        .map(this::toDto)
                        .toList())
                .orElseGet(List::of);
    }

    @Transactional(readOnly = true)
    public List<AIChatMessage> getRecentMessages(UUID userId, int limit) {
        User user = findUser(userId);
        List<AIChatMessage> recentMessages = findCurrentDailySession(user)
                .map(session -> aiChatMessageRepository.findTop20BySessionIdOrderByCreatedAtDescIdDesc(session.getId()))
                .orElseGet(List::of);
        if (recentMessages.size() <= limit) {
            return recentMessages;
        }
        return recentMessages.subList(0, limit);
    }

    private java.util.Optional<ChatSession> findCurrentDailySession(User user) {
        LocalDate today = LocalDate.now();
        return chatSessionRepository.findTopByUserIdOrderByLastActivityAtDescIdDesc(user.getId())
                .filter(existing -> existing.getCreatedAt() != null && existing.getCreatedAt().toLocalDate().isEqual(today));
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found."));
    }

    private ChatSession getOrCreateActiveSession(User user) {
        LocalDateTime now = LocalDateTime.now();
        return findCurrentDailySession(user)
                .map(existing -> touchSession(existing, now))
                .orElseGet(() -> chatSessionRepository.save(ChatSession.builder()
                        .user(user)
                        .lastActivityAt(now)
                        .build()));
    }

    private ChatSession touchSession(ChatSession session, LocalDateTime now) {
        session.setLastActivityAt(now);
        return chatSessionRepository.save(session);
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
