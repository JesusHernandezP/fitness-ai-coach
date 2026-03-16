package com.fitness.fitnessaicoach.repository;

import com.fitness.fitnessaicoach.domain.Goal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface GoalRepository extends JpaRepository<Goal, UUID> {

    Optional<Goal> findTopByUserIdOrderByIdDesc(UUID userId);
}
