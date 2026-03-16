package com.fitness.fitnessaicoach.service;

import com.fitness.fitnessaicoach.domain.DailyLog;
import com.fitness.fitnessaicoach.domain.Meal;
import com.fitness.fitnessaicoach.dto.MealRequest;
import com.fitness.fitnessaicoach.dto.MealResponse;
import com.fitness.fitnessaicoach.exception.DailyLogNotFoundException;
import com.fitness.fitnessaicoach.exception.MealNotFoundException;
import com.fitness.fitnessaicoach.repository.DailyLogRepository;
import com.fitness.fitnessaicoach.repository.MealRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MealService {

    private final MealRepository mealRepository;
    private final DailyLogRepository dailyLogRepository;

    public MealResponse createMeal(MealRequest request) {
        DailyLog dailyLog = dailyLogRepository.findById(request.getDailyLogId())
                .orElseThrow(() -> new DailyLogNotFoundException("Daily log not found."));

        Meal meal = Meal.builder()
                .mealType(request.getMealType())
                .dailyLog(dailyLog)
                .build();

        Meal saved = mealRepository.save(meal);

        return toResponse(saved);
    }

    public List<MealResponse> getAllMeals() {
        return mealRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public MealResponse getMealById(UUID id) {
        Meal meal = mealRepository.findById(id)
                .orElseThrow(() -> new MealNotFoundException("Meal not found."));

        return toResponse(meal);
    }

    public void deleteMeal(UUID id) {
        Meal meal = mealRepository.findById(id)
                .orElseThrow(() -> new MealNotFoundException("Meal not found."));

        mealRepository.delete(meal);
    }

    private MealResponse toResponse(Meal meal) {
        return MealResponse.builder()
                .id(meal.getId())
                .mealType(meal.getMealType())
                .dailyLogId(meal.getDailyLog() != null ? meal.getDailyLog().getId() : null)
                .build();
    }
}
