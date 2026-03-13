package com.fitness.fitnessaicoach.controller;

import com.fitness.fitnessaicoach.dto.FoodRequest;
import com.fitness.fitnessaicoach.dto.FoodResponse;
import com.fitness.fitnessaicoach.service.FoodService;
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
@RequestMapping("/api/foods")
@RequiredArgsConstructor
@Tag(name = "Foods", description = "Food management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class FoodController {

    private final FoodService foodService;

    @PostMapping
    @Operation(summary = "Create a new food")
    public ResponseEntity<FoodResponse> createFood(@Valid @RequestBody FoodRequest request) {
        FoodResponse response = foodService.createFood(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all foods")
    public List<FoodResponse> getAllFoods() {
        return foodService.getAllFoods();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get food by id")
    public ResponseEntity<FoodResponse> getFoodById(@PathVariable UUID id) {
        FoodResponse response = foodService.getFoodById(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete food by id")
    public ResponseEntity<Void> deleteFood(@PathVariable UUID id) {
        foodService.deleteFood(id);
        return ResponseEntity.noContent().build();
    }
}
