package com.fitness.fitnessaicoach.repository;

import com.fitness.fitnessaicoach.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    List<ChatMessage> findTop20BySessionIdOrderByCreatedAtDescIdDesc(UUID sessionId);

    List<ChatMessage> findBySessionIdOrderByCreatedAtAscIdAsc(UUID sessionId);
}
