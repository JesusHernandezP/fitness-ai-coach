package com.fitness.fitnessaicoach.service;

import com.fitness.fitnessaicoach.domain.Exercise;
import com.fitness.fitnessaicoach.dto.ExerciseRequest;
import com.fitness.fitnessaicoach.dto.ExerciseResponse;
import com.fitness.fitnessaicoach.repository.ExerciseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;

    public ExerciseResponse createExercise(ExerciseRequest request) {

        Exercise exercise = Exercise.builder()
                .name(request.getName())
                .muscleGroup(request.getMuscleGroup())
                .equipment(request.getEquipment())
                .description(request.getDescription())
                .build();

        Exercise saved = exerciseRepository.save(exercise);

        return ExerciseResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .muscleGroup(saved.getMuscleGroup())
                .equipment(saved.getEquipment())
                .description(saved.getDescription())
                .build();
    }

    public List<ExerciseResponse> getAllExercises() {
        return exerciseRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ExerciseResponse> getExercisesByMuscleGroup(String muscleGroup) {

        return exerciseRepository.findByMuscleGroupIgnoreCase(muscleGroup)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ExerciseResponse> searchExercises(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        String trimmedQuery = query.trim();

        if (trimmedQuery.isEmpty()) {
            return List.of();
        }

        return exerciseRepository.findByNameContainingIgnoreCase(trimmedQuery).stream()
                .sorted(Comparator.comparing(Exercise::getName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toResponse)
                .toList();
    }

    private ExerciseResponse toResponse(Exercise exercise) {
        return ExerciseResponse.builder()
                .id(exercise.getId())
                .name(exercise.getName())
                .muscleGroup(exercise.getMuscleGroup())
                .equipment(exercise.getEquipment())
                .description(exercise.getDescription())
                .build();
    }

}
