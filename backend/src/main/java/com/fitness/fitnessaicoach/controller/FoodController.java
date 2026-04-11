package com.fitness.fitnessaicoach.controller;

<<<<<<< HEAD
=======
import com.fitness.fitnessaicoach.dto.ApiResponse;
>>>>>>> main
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
<<<<<<< HEAD
    public ResponseEntity<FoodResponse> createFood(@Valid @RequestBody FoodRequest request) {
        FoodResponse response = foodService.createFood(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
=======
    public ResponseEntity<ApiResponse<FoodResponse>> createFood(@Valid @RequestBody FoodRequest request) {
        FoodResponse response = foodService.createFood(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(HttpStatus.CREATED.value(), response));
>>>>>>> main
    }

    @GetMapping
    @Operation(summary = "Get all foods")
<<<<<<< HEAD
    public List<FoodResponse> getAllFoods() {
        return foodService.getAllFoods();
=======
    public ResponseEntity<ApiResponse<List<FoodResponse>>> getAllFoods() {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), foodService.getAllFoods()));
>>>>>>> main
    }

    @GetMapping("/search")
    @Operation(summary = "Search foods by name")
<<<<<<< HEAD
    public ResponseEntity<List<FoodResponse>> searchFoods(@RequestParam(required = false) String query) {
        List<FoodResponse> response = foodService.searchFoods(query);
        return ResponseEntity.ok(response);
=======
    public ResponseEntity<ApiResponse<List<FoodResponse>>> searchFoods(@RequestParam(required = false) String query) {
        List<FoodResponse> response = foodService.searchFoods(query);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), response));
>>>>>>> main
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get food by id")
<<<<<<< HEAD
    public ResponseEntity<FoodResponse> getFoodById(@PathVariable UUID id) {
        FoodResponse response = foodService.getFoodById(id);
        return ResponseEntity.ok(response);
=======
    public ResponseEntity<ApiResponse<FoodResponse>> getFoodById(@PathVariable UUID id) {
        FoodResponse response = foodService.getFoodById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), response));
>>>>>>> main
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete food by id")
<<<<<<< HEAD
    public ResponseEntity<Void> deleteFood(@PathVariable UUID id) {
        foodService.deleteFood(id);
        return ResponseEntity.noContent().build();
=======
    public ResponseEntity<ApiResponse<Void>> deleteFood(@PathVariable UUID id) {
        foodService.deleteFood(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), null));
>>>>>>> main
    }
}
