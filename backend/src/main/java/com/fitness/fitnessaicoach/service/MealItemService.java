package com.fitness.fitnessaicoach.service;

import com.fitness.fitnessaicoach.domain.Food;
import com.fitness.fitnessaicoach.domain.Meal;
import com.fitness.fitnessaicoach.domain.MealItem;
import com.fitness.fitnessaicoach.dto.FoodRequest;
import com.fitness.fitnessaicoach.dto.MealItemRequest;
import com.fitness.fitnessaicoach.dto.MealItemResponse;
import com.fitness.fitnessaicoach.dto.ai.AIFoodEstimateResponse;
import com.fitness.fitnessaicoach.exception.FoodNotFoundException;
import com.fitness.fitnessaicoach.exception.MealItemNotFoundException;
import com.fitness.fitnessaicoach.exception.MealNotFoundException;
import com.fitness.fitnessaicoach.repository.DailyLogRepository;
import com.fitness.fitnessaicoach.repository.FoodRepository;
import com.fitness.fitnessaicoach.repository.MealItemRepository;
import com.fitness.fitnessaicoach.repository.MealRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MealItemService {

    private final MealItemRepository mealItemRepository;
    private final MealRepository mealRepository;
    private final FoodRepository foodRepository;
    private final DailyLogRepository dailyLogRepository;
    private final AIFoodEstimationService aiFoodEstimationService;
    private final NutritionMath nutritionMath;

    @Transactional
    public MealItemResponse createMealItem(MealItemRequest request) {
        Meal meal = mealRepository.findById(request.getMealId())
                .orElseThrow(() -> new MealNotFoundException("Meal not found."));

        Food food = resolveFood(request);

        Double calculatedCalories = nutritionMath.caloriesFor(food, request.getQuantity());

        MealItem mealItem = MealItem.builder()
                .meal(meal)
                .food(food)
                .quantity(request.getQuantity())
                .calculatedCalories(calculatedCalories)
                .build();

        MealItem saved = mealItemRepository.save(mealItem);
        syncDailyLogCaloriesConsumed(meal.getDailyLog().getId());

        return toResponse(saved);
    }

    public List<MealItemResponse> getAllMealItems() {
        return mealItemRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void deleteMealItem(UUID id) {
        MealItem mealItem = mealItemRepository.findById(id)
                .orElseThrow(() -> new MealItemNotFoundException("Meal item not found."));

        UUID dailyLogId = mealItem.getMeal().getDailyLog().getId();
        mealItemRepository.delete(mealItem);
        syncDailyLogCaloriesConsumed(dailyLogId);
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

    private Food resolveFood(MealItemRequest request) {
        if (request.getFoodId() != null) {
            return foodRepository.findById(request.getFoodId())
                    .orElseThrow(() -> new FoodNotFoundException("Food not found."));
        }

        String foodName = normalizeFoodName(request.getFoodName());
        return foodRepository.findFirstByNameIgnoreCase(foodName)
                .orElseGet(() -> createFoodFromAiEstimate(foodName));
    }

    private Food createFoodFromAiEstimate(String foodName) {
        AIFoodEstimateResponse estimate = aiFoodEstimationService.estimateFood(foodName);
        FoodRequest foodRequest = new FoodRequest();
        foodRequest.setName(normalizeFoodName(estimate.getName()));
        foodRequest.setCalories(estimate.getCalories());
        foodRequest.setProtein(estimate.getProtein());
        foodRequest.setCarbs(estimate.getCarbs());
        foodRequest.setFat(estimate.getFat());

        Food food = new Food(
                foodRequest.getName(),
                foodRequest.getCalories(),
                foodRequest.getProtein(),
                foodRequest.getCarbs(),
                foodRequest.getFat()
        );
        return foodRepository.save(food);
    }

    private String normalizeFoodName(String foodName) {
        if (foodName == null || foodName.isBlank()) {
            throw new FoodNotFoundException("Food not found.");
        }
        return foodName.trim().replaceAll("\\s+", " ");
    }

    private void syncDailyLogCaloriesConsumed(UUID dailyLogId) {
        dailyLogRepository.findById(dailyLogId).ifPresent(dailyLog -> {
            dailyLog.setCaloriesConsumed(Objects.requireNonNullElse(
                    mealItemRepository.sumCalculatedCaloriesByDailyLogId(dailyLogId),
                    0.0
            ));
            dailyLogRepository.save(dailyLog);
        });
    }
}
