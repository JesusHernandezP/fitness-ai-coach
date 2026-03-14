package com.fitness.fitnessaicoach.repository;

import com.fitness.fitnessaicoach.domain.WorkoutSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WorkoutSessionRepository extends JpaRepository<WorkoutSession, UUID> {
}
