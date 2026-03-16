package com.fitness.fitnessaicoach.repository;

import com.fitness.fitnessaicoach.domain.Meal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MealRepository extends JpaRepository<Meal, UUID> {

    long countByDailyLogId(UUID dailyLogId);
}
