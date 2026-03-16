package com.fitness.fitnessaicoach.repository;

import com.fitness.fitnessaicoach.domain.BodyMetrics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BodyMetricsRepository extends JpaRepository<BodyMetrics, UUID> {

    Optional<BodyMetrics> findTopByUserIdOrderByDateDescIdDesc(UUID userId);
}
