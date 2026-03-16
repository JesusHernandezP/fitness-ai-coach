package com.fitness.fitnessaicoach.controller;

import com.fitness.fitnessaicoach.dto.GoalRequest;
import com.fitness.fitnessaicoach.dto.GoalResponse;
import com.fitness.fitnessaicoach.service.GoalService;
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
@RequestMapping("/api/goals")
@RequiredArgsConstructor
@Tag(name = "Goals", description = "Goals management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class GoalController {

    private final GoalService goalService;

    @PostMapping
    @Operation(summary = "Create a new goal")
    public ResponseEntity<GoalResponse> createGoal(@Valid @RequestBody GoalRequest request) {
        GoalResponse response = goalService.createGoal(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all goals")
    public List<GoalResponse> getAllGoals() {
        return goalService.getAllGoals();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get goal by id")
    public ResponseEntity<GoalResponse> getGoalById(@PathVariable UUID id) {
        GoalResponse response = goalService.getGoalById(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete goal by id")
    public ResponseEntity<Void> deleteGoal(@PathVariable UUID id) {
        goalService.deleteGoal(id);
        return ResponseEntity.noContent().build();
    }
}
