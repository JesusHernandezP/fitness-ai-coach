package com.fitness.fitnessaicoach.repository;

import com.fitness.fitnessaicoach.domain.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ExerciseRepository extends JpaRepository<Exercise, UUID> {
}