package com.fitness.fitnessaicoach.repository;

import com.fitness.fitnessaicoach.domain.Food;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FoodRepository extends JpaRepository<Food, UUID> {

<<<<<<< HEAD
    java.util.Optional<Food> findFirstByNameIgnoreCase(String name);

=======
>>>>>>> main
    List<Food> findByNameContainingIgnoreCase(String query);
}
 
