package com.fitness.fitnessaicoach.controller;

import com.fitness.fitnessaicoach.dto.ApiResponse;
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
    public ResponseEntity<ApiResponse<FoodResponse>> createFood(@Valid @RequestBody FoodRequest request) {
        FoodResponse response = foodService.createFood(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(HttpStatus.CREATED.value(), response));
    }

    @GetMapping
    @Operation(summary = "Get all foods")
    public ResponseEntity<ApiResponse<List<FoodResponse>>> getAllFoods() {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), foodService.getAllFoods()));
    }

    @GetMapping("/search")
    @Operation(summary = "Search foods by name")
    public ResponseEntity<ApiResponse<List<FoodResponse>>> searchFoods(@RequestParam(required = false) String query) {
        List<FoodResponse> response = foodService.searchFoods(query);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get food by id")
    public ResponseEntity<ApiResponse<FoodResponse>> getFoodById(@PathVariable UUID id) {
        FoodResponse response = foodService.getFoodById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete food by id")
    public ResponseEntity<ApiResponse<Void>> deleteFood(@PathVariable UUID id) {
        foodService.deleteFood(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), null));
    }
}
