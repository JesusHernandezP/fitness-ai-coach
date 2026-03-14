package com.fitness.fitnessaicoach.controller;

import com.fitness.fitnessaicoach.dto.MealItemRequest;
import com.fitness.fitnessaicoach.dto.MealItemResponse;
import com.fitness.fitnessaicoach.service.MealItemService;
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
@RequestMapping("/api/meal-items")
@RequiredArgsConstructor
@Tag(name = "Meal Items", description = "Meal item management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class MealItemController {

    private final MealItemService mealItemService;

    @PostMapping
    @Operation(summary = "Create a new meal item")
    public ResponseEntity<MealItemResponse> createMealItem(@Valid @RequestBody MealItemRequest request) {
        MealItemResponse response = mealItemService.createMealItem(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all meal items")
    public List<MealItemResponse> getAllMealItems() {
        return mealItemService.getAllMealItems();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete meal item by id")
    public ResponseEntity<Void> deleteMealItem(@PathVariable UUID id) {
        mealItemService.deleteMealItem(id);
        return ResponseEntity.noContent().build();
    }
}
