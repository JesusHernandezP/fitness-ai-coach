package com.fitness.fitnessaicoach.controller;

<<<<<<< HEAD
=======
import com.fitness.fitnessaicoach.dto.ApiResponse;
>>>>>>> main
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
<<<<<<< HEAD
import org.springframework.security.core.Authentication;
=======
>>>>>>> main
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
<<<<<<< HEAD
    public ResponseEntity<GoalResponse> createGoal(
            Authentication authentication,
            @Valid @RequestBody GoalRequest request) {

        GoalResponse response = goalService.createGoal(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
=======
    public ResponseEntity<ApiResponse<GoalResponse>> createGoal(@Valid @RequestBody GoalRequest request) {
        GoalResponse response = goalService.createGoal(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(HttpStatus.CREATED.value(), response));
>>>>>>> main
    }

    @GetMapping
    @Operation(summary = "Get all goals")
<<<<<<< HEAD
    public List<GoalResponse> getAllGoals(Authentication authentication) {
        return goalService.getAllGoals(authentication.getName());
=======
    public ResponseEntity<ApiResponse<List<GoalResponse>>> getAllGoals() {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), goalService.getAllGoals()));
>>>>>>> main
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get goal by id")
<<<<<<< HEAD
    public ResponseEntity<GoalResponse> getGoalById(
            Authentication authentication,
            @PathVariable UUID id) {

        GoalResponse response = goalService.getGoalById(authentication.getName(), id);
        return ResponseEntity.ok(response);
=======
    public ResponseEntity<ApiResponse<GoalResponse>> getGoalById(@PathVariable UUID id) {
        GoalResponse response = goalService.getGoalById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), response));
>>>>>>> main
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete goal by id")
<<<<<<< HEAD
    public ResponseEntity<Void> deleteGoal(
            Authentication authentication,
            @PathVariable UUID id) {

        goalService.deleteGoal(authentication.getName(), id);
        return ResponseEntity.noContent().build();
=======
    public ResponseEntity<ApiResponse<Void>> deleteGoal(@PathVariable UUID id) {
        goalService.deleteGoal(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), null));
>>>>>>> main
    }
}
