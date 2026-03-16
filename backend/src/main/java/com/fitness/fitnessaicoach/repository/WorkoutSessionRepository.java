package com.fitness.fitnessaicoach.repository;

import com.fitness.fitnessaicoach.domain.WorkoutSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface WorkoutSessionRepository extends JpaRepository<WorkoutSession, UUID> {

    List<WorkoutSession> findByDailyLogId(UUID dailyLogId);

    long countByDailyLogId(UUID dailyLogId);

    @Query("SELECT COALESCE(SUM(ws.caloriesBurned), 0) FROM WorkoutSession ws WHERE ws.dailyLog.id = :dailyLogId")
    Double sumCaloriesBurnedByDailyLogId(@Param("dailyLogId") UUID dailyLogId);
}
