package com.fitness.fitnessaicoach.repository;

import com.fitness.fitnessaicoach.domain.Food;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FoodRepository extends JpaRepository<Food, UUID> {

    java.util.Optional<Food> findFirstByNameIgnoreCase(String name);

    List<Food> findByNameContainingIgnoreCase(String query);
}
 
