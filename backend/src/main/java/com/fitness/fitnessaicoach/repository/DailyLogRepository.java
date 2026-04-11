package com.fitness.fitnessaicoach.repository;

import com.fitness.fitnessaicoach.domain.DailyLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DailyLogRepository extends JpaRepository<DailyLog, UUID> {

    List<DailyLog> findByUserId(UUID userId);

<<<<<<< HEAD
    Optional<DailyLog> findTopByUserIdOrderByLogDateDescIdDesc(UUID userId);

=======
>>>>>>> main
    Optional<DailyLog> findByUserIdAndLogDate(UUID userId, LocalDate date);
}
