package com.fitness.fitnessaicoach.repository;

import com.fitness.fitnessaicoach.domain.MealItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MealItemRepository extends JpaRepository<MealItem, UUID> {
}
