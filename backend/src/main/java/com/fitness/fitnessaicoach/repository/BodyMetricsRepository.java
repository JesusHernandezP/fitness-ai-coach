package com.fitness.fitnessaicoach.repository;

import com.fitness.fitnessaicoach.domain.BodyMetrics;
import org.springframework.data.jpa.repository.JpaRepository;

<<<<<<< HEAD
import java.time.LocalDate;
import java.util.List;
=======
>>>>>>> main
import java.util.Optional;
import java.util.UUID;

public interface BodyMetricsRepository extends JpaRepository<BodyMetrics, UUID> {

    Optional<BodyMetrics> findTopByUserIdOrderByDateDescIdDesc(UUID userId);
<<<<<<< HEAD

    List<BodyMetrics> findAllByUserIdOrderByDateDescIdDesc(UUID userId);

    List<BodyMetrics> findByUserIdOrderByDateAsc(UUID userId);

    Optional<BodyMetrics> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByUserIdAndDate(UUID userId, LocalDate date);
=======
>>>>>>> main
}
