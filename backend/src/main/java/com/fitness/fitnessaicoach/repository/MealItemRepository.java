package com.fitness.fitnessaicoach.repository;

import com.fitness.fitnessaicoach.domain.MealItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface MealItemRepository extends JpaRepository<MealItem, UUID> {

    @Query("SELECT COALESCE(SUM(mi.calculatedCalories), 0) FROM MealItem mi WHERE mi.meal.dailyLog.id = :dailyLogId")
    Double sumCalculatedCaloriesByDailyLogId(@Param("dailyLogId") UUID dailyLogId);
}
