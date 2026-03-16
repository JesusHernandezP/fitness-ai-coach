package com.fitness.fitnessaicoach.service;

import com.fitness.fitnessaicoach.domain.Food;
import com.fitness.fitnessaicoach.domain.Meal;
import com.fitness.fitnessaicoach.domain.MealItem;
import com.fitness.fitnessaicoach.dto.MealItemRequest;
import com.fitness.fitnessaicoach.dto.MealItemResponse;
import com.fitness.fitnessaicoach.exception.FoodNotFoundException;
import com.fitness.fitnessaicoach.exception.MealItemNotFoundException;
import com.fitness.fitnessaicoach.exception.MealNotFoundException;
import com.fitness.fitnessaicoach.repository.FoodRepository;
import com.fitness.fitnessaicoach.repository.MealItemRepository;
import com.fitness.fitnessaicoach.repository.MealRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MealItemService {

    private final MealItemRepository mealItemRepository;
    private final MealRepository mealRepository;
    private final FoodRepository foodRepository;

    public MealItemResponse createMealItem(MealItemRequest request) {
        Meal meal = mealRepository.findById(request.getMealId())
                .orElseThrow(() -> new MealNotFoundException("Meal not found."));

        Food food = foodRepository.findById(request.getFoodId())
                .orElseThrow(() -> new FoodNotFoundException("Food not found."));

        Double calculatedCalories = food.getCalories() * request.getQuantity();

        MealItem mealItem = MealItem.builder()
                .meal(meal)
                .food(food)
                .quantity(request.getQuantity())
                .calculatedCalories(calculatedCalories)
                .build();

        MealItem saved = mealItemRepository.save(mealItem);

        return toResponse(saved);
    }

    public List<MealItemResponse> getAllMealItems() {
        return mealItemRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public void deleteMealItem(UUID id) {
        MealItem mealItem = mealItemRepository.findById(id)
                .orElseThrow(() -> new MealItemNotFoundException("Meal item not found."));

        mealItemRepository.delete(mealItem);
    }

    private MealItemResponse toResponse(MealItem mealItem) {
        return MealItemResponse.builder()
                .id(mealItem.getId())
                .mealId(mealItem.getMeal() != null ? mealItem.getMeal().getId() : null)
                .foodId(mealItem.getFood() != null ? mealItem.getFood().getId() : null)
                .quantity(mealItem.getQuantity())
                .calculatedCalories(mealItem.getCalculatedCalories())
                .build();
    }
}
