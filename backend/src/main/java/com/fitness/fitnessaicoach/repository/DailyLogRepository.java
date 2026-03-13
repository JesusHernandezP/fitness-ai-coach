package com.fitness.fitnessaicoach.repository;

import com.fitness.fitnessaicoach.domain.DailyLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DailyLogRepository extends JpaRepository<DailyLog, UUID> {
}
