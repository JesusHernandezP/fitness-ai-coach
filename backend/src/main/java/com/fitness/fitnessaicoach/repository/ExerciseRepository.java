package com.fitness.fitnessaicoach.repository;

import com.fitness.fitnessaicoach.domain.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ExerciseRepository extends JpaRepository<Exercise, UUID> {
<<<<<<< HEAD
    java.util.Optional<Exercise> findFirstByNameIgnoreCase(String name);
=======
>>>>>>> main
    List<Exercise> findByMuscleGroupIgnoreCase(String muscleGroup);
    List<Exercise> findByNameContainingIgnoreCase(String query);

}
