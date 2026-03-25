package com.fitness.fitnessaicoach.controller;

import com.fitness.fitnessaicoach.dto.WorkoutSessionRequest;
import com.fitness.fitnessaicoach.dto.WorkoutSessionResponse;
import com.fitness.fitnessaicoach.service.WorkoutSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workouts")
@RequiredArgsConstructor
@Tag(name = "Workouts", description = "Workout session management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class WorkoutSessionController {

    private final WorkoutSessionService workoutSessionService;

    @PostMapping
    @Operation(summary = "Create a new workout session")
    public ResponseEntity<WorkoutSessionResponse> createWorkoutSession(
            @Valid @RequestBody WorkoutSessionRequest request) {

        WorkoutSessionResponse response = workoutSessionService.createWorkoutSession(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all workout sessions")
    public List<WorkoutSessionResponse> getAllWorkoutSessions() {
        return workoutSessionService.getAllWorkoutSessions();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get workout session by id")
    public ResponseEntity<WorkoutSessionResponse> getWorkoutSessionById(@PathVariable UUID id) {
        WorkoutSessionResponse response = workoutSessionService.getWorkoutSessionById(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete workout session by id")
    public ResponseEntity<Void> deleteWorkoutSession(@PathVariable UUID id) {
        workoutSessionService.deleteWorkoutSession(id);
        return ResponseEntity.noContent().build();
    }
}
