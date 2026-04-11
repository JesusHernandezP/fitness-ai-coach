package com.fitness.fitnessaicoach.repository;

import com.fitness.fitnessaicoach.domain.Food;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FoodRepository extends JpaRepository<Food, UUID> {

    List<Food> findByNameContainingIgnoreCase(String query);

    Optional<Food> findFirstByNameIgnoreCase(String name);
}
