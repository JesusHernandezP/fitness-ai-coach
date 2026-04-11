package com.fitness.fitnessaicoach.controller;

import com.fitness.fitnessaicoach.dto.ApiResponse;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<ApiResponse<GoalResponse>> createGoal(
            Authentication authentication,
            @Valid @RequestBody GoalRequest request
    ) {
        GoalResponse response = goalService.createGoal(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), response));
    }

    @GetMapping
    @Operation(summary = "Get all goals")
    public ResponseEntity<ApiResponse<List<GoalResponse>>> getAllGoals(Authentication authentication) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), goalService.getAllGoals(authentication.getName())));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get goal by id")
    public ResponseEntity<ApiResponse<GoalResponse>> getGoalById(
            Authentication authentication,
            @PathVariable UUID id
    ) {
        GoalResponse response = goalService.getGoalById(authentication.getName(), id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete goal by id")
    public ResponseEntity<ApiResponse<Void>> deleteGoal(
            Authentication authentication,
            @PathVariable UUID id
    ) {
        goalService.deleteGoal(authentication.getName(), id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), null));
    }
}
