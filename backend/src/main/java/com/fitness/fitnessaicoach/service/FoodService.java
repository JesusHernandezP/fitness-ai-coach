package com.fitness.fitnessaicoach.service;

import com.fitness.fitnessaicoach.domain.Food;
import com.fitness.fitnessaicoach.dto.FoodRequest;
import com.fitness.fitnessaicoach.dto.FoodResponse;
import com.fitness.fitnessaicoach.exception.FoodNotFoundException;
import com.fitness.fitnessaicoach.repository.FoodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FoodService {

    private final FoodRepository foodRepository;

    public FoodResponse createFood(FoodRequest request) {

        Food food = new Food(
                request.getName(),
                request.getCalories(),
                request.getProtein(),
                request.getCarbs(),
                request.getFat()
        );

        Food saved = foodRepository.save(food);

        return toResponse(saved);
    }

    public List<FoodResponse> getAllFoods() {
        return foodRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public FoodResponse getFoodById(UUID id) {
        Food food = foodRepository.findById(id)
                .orElseThrow(() -> new FoodNotFoundException("Food not found."));

        return toResponse(food);
    }

    public void deleteFood(UUID id) {
        Food food = foodRepository.findById(id)
                .orElseThrow(() -> new FoodNotFoundException("Food not found."));

        foodRepository.delete(food);
    }

    private FoodResponse toResponse(Food food) {
        return FoodResponse.builder()
                .id(food.getId())
                .name(food.getName())
                .calories(food.getCalories())
                .protein(food.getProtein())
                .carbs(food.getCarbs())
                .fat(food.getFat())
                .build();
    }
}
