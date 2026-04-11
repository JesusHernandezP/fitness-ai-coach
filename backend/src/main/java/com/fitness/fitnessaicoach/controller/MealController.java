package com.fitness.fitnessaicoach.controller;

import com.fitness.fitnessaicoach.dto.MealRequest;
import com.fitness.fitnessaicoach.dto.MealResponse;
import com.fitness.fitnessaicoach.service.MealService;
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
@RequestMapping("/api/meals")
@RequiredArgsConstructor
@Tag(name = "Meals", description = "Meal management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class MealController {

    private final MealService mealService;

    @PostMapping
    @Operation(summary = "Create a new meal")
    public ResponseEntity<MealResponse> createMeal(@Valid @RequestBody MealRequest request) {
        MealResponse response = mealService.createMeal(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all meals")
    public List<MealResponse> getAllMeals() {
        return mealService.getAllMeals();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get meal by id")
    public ResponseEntity<MealResponse> getMealById(@PathVariable UUID id) {
        MealResponse response = mealService.getMealById(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete meal by id")
    public ResponseEntity<Void> deleteMeal(@PathVariable UUID id) {
        mealService.deleteMeal(id);
        return ResponseEntity.noContent().build();
    }
}
