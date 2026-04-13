package com.fitness.fitnessaicoach.repository;

import com.fitness.fitnessaicoach.domain.AIChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AIChatMessageRepository extends JpaRepository<AIChatMessage, UUID> {

    List<AIChatMessage> findTop20BySessionIdOrderByCreatedAtDescIdDesc(UUID sessionId);

    List<AIChatMessage> findBySessionIdOrderByCreatedAtAscIdAsc(UUID sessionId);

    List<AIChatMessage> findTop20BySessionUserIdOrderByCreatedAtDescIdDesc(UUID userId);

    List<AIChatMessage> findTop8BySessionIdOrderByCreatedAtDescIdDesc(UUID sessionId);
}
