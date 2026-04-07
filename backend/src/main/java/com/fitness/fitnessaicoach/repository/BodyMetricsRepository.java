package com.fitness.fitnessaicoach.repository;

import com.fitness.fitnessaicoach.domain.BodyMetrics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BodyMetricsRepository extends JpaRepository<BodyMetrics, UUID> {

    Optional<BodyMetrics> findTopByUserIdOrderByDateDescIdDesc(UUID userId);

    List<BodyMetrics> findAllByUserIdOrderByDateDescIdDesc(UUID userId);

    List<BodyMetrics> findByUserIdOrderByDateAsc(UUID userId);

    Optional<BodyMetrics> findByIdAndUserId(UUID id, UUID userId);

    Optional<BodyMetrics> findByUserIdAndDate(UUID userId, LocalDate date);

    boolean existsByUserIdAndDate(UUID userId, LocalDate date);
}
