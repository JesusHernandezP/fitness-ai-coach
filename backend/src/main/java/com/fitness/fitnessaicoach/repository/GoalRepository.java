package com.fitness.fitnessaicoach.repository;

import com.fitness.fitnessaicoach.domain.Goal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GoalRepository extends JpaRepository<Goal, UUID> {

    List<Goal> findAllByUserIdOrderByCreatedAtDescIdDesc(UUID userId);

    Optional<Goal> findByIdAndUserId(UUID id, UUID userId);

    Optional<Goal> findTopByUserIdOrderByCreatedAtDescIdDesc(UUID userId);

    boolean existsByUserIdAndCreatedAtBetween(UUID userId, LocalDateTime start, LocalDateTime end);
}
