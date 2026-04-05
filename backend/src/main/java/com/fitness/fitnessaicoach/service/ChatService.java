package com.fitness.fitnessaicoach.service;

import com.fitness.fitnessaicoach.ai.provider.AITextGenerationClient;
import com.fitness.fitnessaicoach.domain.BodyMetrics;
import com.fitness.fitnessaicoach.domain.ChatMessage;
import com.fitness.fitnessaicoach.domain.ChatRole;
import com.fitness.fitnessaicoach.domain.ChatSession;
import com.fitness.fitnessaicoach.domain.DailyLog;
import com.fitness.fitnessaicoach.domain.Goal;
import com.fitness.fitnessaicoach.domain.User;
import com.fitness.fitnessaicoach.dto.ai.AIChatMessageResponse;
import com.fitness.fitnessaicoach.exception.UserNotFoundException;
import com.fitness.fitnessaicoach.repository.BodyMetricsRepository;
import com.fitness.fitnessaicoach.repository.ChatMessageRepository;
import com.fitness.fitnessaicoach.repository.ChatSessionRepository;
import com.fitness.fitnessaicoach.repository.DailyLogRepository;
import com.fitness.fitnessaicoach.repository.GoalRepository;
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
public class ChatService {

    private static final int MAX_STORED_MESSAGES = 20;

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final GoalRepository goalRepository;
    private final DailyLogRepository dailyLogRepository;
    private final BodyMetricsRepository bodyMetricsRepository;
    private final AITextGenerationClient aiTextGenerationClient;

    @Transactional
    public AIChatMessageResponse sendMessage(String email, String messageContent) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found."));

        ChatSession session = getOrCreateActiveSession(user);
        saveMessage(session, ChatRole.USER, messageContent);

        String prompt = buildPrompt(user.getId(), session, messageContent);
        String reply = aiTextGenerationClient.generateText(prompt).trim();

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
        chatMessageRepository.save(ChatMessage.builder()
                .session(session)
                .role(role)
                .content(content)
                .build());
    }

    private String buildPrompt(UUID userId, ChatSession session, String latestUserMessage) {
        Goal latestGoal = goalRepository.findTopByUserIdOrderByCreatedAtDescIdDesc(userId).orElse(null);
        DailyLog latestDailyLog = dailyLogRepository.findTopByUserIdOrderByLogDateDescIdDesc(userId).orElse(null);
        BodyMetrics latestBodyMetrics = bodyMetricsRepository.findTopByUserIdOrderByDateDescIdDesc(userId).orElse(null);

        List<ChatMessage> recentMessages = chatMessageRepository.findTop20BySessionIdOrderByCreatedAtDescIdDesc(session.getId());
        Collections.reverse(recentMessages);

        return """
                You are a continuous AI fitness coach in an ongoing conversation.

                Current user context:
                - goalType: %s
                - targetCalories: %s
                - caloriesConsumed: %s
                - caloriesBurned: %s
                - calorieBalance: %s
                - steps: %s
                - latestWeight: %s

                Conversation rules:
                - Be supportive, clear, and actionable.
                - Keep replies short: at most 4 sentences.
                - Avoid repetition.
                - Use the recent conversation and the latest fitness context.
                - If data is missing, say so briefly and still help.

                Recent conversation:
                %s

                Latest user message:
                %s
                """.formatted(
                latestGoal != null && latestGoal.getGoalType() != null ? latestGoal.getGoalType().name() : "UNKNOWN",
                latestGoal != null && latestGoal.getTargetCalories() != null ? latestGoal.getTargetCalories() : "unknown",
                latestDailyLog != null && latestDailyLog.getCaloriesConsumed() != null ? latestDailyLog.getCaloriesConsumed() : 0.0,
                latestDailyLog != null && latestDailyLog.getCaloriesBurned() != null ? latestDailyLog.getCaloriesBurned() : 0.0,
                calculateBalance(latestDailyLog),
                latestDailyLog != null && latestDailyLog.getSteps() != null ? latestDailyLog.getSteps() : 0,
                latestBodyMetrics != null && latestBodyMetrics.getWeight() != null ? latestBodyMetrics.getWeight() : "unknown",
                formatConversation(recentMessages),
                latestUserMessage
        );
    }

    private double calculateBalance(DailyLog latestDailyLog) {
        if (latestDailyLog == null) {
            return 0.0;
        }

        double consumed = latestDailyLog.getCaloriesConsumed() != null ? latestDailyLog.getCaloriesConsumed() : 0.0;
        double burned = latestDailyLog.getCaloriesBurned() != null ? latestDailyLog.getCaloriesBurned() : 0.0;
        return consumed - burned;
    }

    private String formatConversation(List<ChatMessage> recentMessages) {
        if (recentMessages.isEmpty()) {
            return "No previous messages.";
        }

        return recentMessages.stream()
                .sorted(Comparator.comparing(ChatMessage::getCreatedAt).thenComparing(ChatMessage::getId))
                .map(message -> message.getRole().name() + ": " + message.getContent())
                .reduce((left, right) -> left + System.lineSeparator() + right)
                .orElse("No previous messages.");
    }

    private void trimStoredMessages(UUID sessionId) {
        List<ChatMessage> allMessages = chatMessageRepository.findBySessionIdOrderByCreatedAtAscIdAsc(sessionId);
        int messagesToDelete = allMessages.size() - MAX_STORED_MESSAGES;
        if (messagesToDelete <= 0) {
            return;
        }

        chatMessageRepository.deleteAll(allMessages.subList(0, messagesToDelete));
    }
}
