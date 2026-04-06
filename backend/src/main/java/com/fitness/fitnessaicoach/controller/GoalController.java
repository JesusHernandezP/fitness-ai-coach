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
import org.springframework.security.core.Authentication;
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
    public ResponseEntity<GoalResponse> createGoal(
            Authentication authentication,
            @Valid @RequestBody GoalRequest request) {

        GoalResponse response = goalService.createGoal(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all goals")
    public List<GoalResponse> getAllGoals(Authentication authentication) {
        return goalService.getAllGoals(authentication.getName());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get goal by id")
    public ResponseEntity<GoalResponse> getGoalById(
            Authentication authentication,
            @PathVariable UUID id) {

        GoalResponse response = goalService.getGoalById(authentication.getName(), id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete goal by id")
    public ResponseEntity<Void> deleteGoal(
            Authentication authentication,
            @PathVariable UUID id) {

        goalService.deleteGoal(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
