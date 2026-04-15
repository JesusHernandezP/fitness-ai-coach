package com.fitness.fitnessaicoach.repository;

import com.fitness.fitnessaicoach.domain.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ChatSessionRepository extends JpaRepository<ChatSession, UUID> {

    Optional<ChatSession> findTopByUserIdOrderByLastActivityAtDescIdDesc(UUID userId);

    Optional<ChatSession> findByIdAndUserId(UUID id, UUID userId);
}
