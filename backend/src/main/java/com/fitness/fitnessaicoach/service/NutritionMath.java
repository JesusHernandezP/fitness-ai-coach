package com.fitness.fitnessaicoach.service;

import com.fitness.fitnessaicoach.domain.Food;
import com.fitness.fitnessaicoach.domain.MealItem;
import org.springframework.stereotype.Component;

@Component
public class NutritionMath {

    public double caloriesFor(MealItem item) {
        if (item == null || item.getFood() == null) {
            return 0.0;
        }
        return caloriesFor(item.getFood(), item.getQuantity());
    }

    public double proteinFor(MealItem item) {
        if (item == null || item.getFood() == null) {
            return 0.0;
        }
        return nutrientFor(item.getFood().getProtein(), item.getQuantity());
    }

    public double carbsFor(MealItem item) {
        if (item == null || item.getFood() == null) {
            return 0.0;
        }
        return nutrientFor(item.getFood().getCarbs(), item.getQuantity());
    }

    public double fatFor(MealItem item) {
        if (item == null || item.getFood() == null) {
            return 0.0;
        }
        return nutrientFor(item.getFood().getFat(), item.getQuantity());
    }

    public double caloriesFor(Food food, Double quantity) {
        if (food == null) {
            return 0.0;
        }
        return nutrientFor(food.getCalories(), quantity);
    }

    public double nutrientFor(Double nutrientPerReferenceServing, Double quantity) {
        if (nutrientPerReferenceServing == null) {
            return 0.0;
        }
        return nutrientPerReferenceServing * servingMultiplier(quantity);
    }

    public double servingMultiplier(Double quantity) {
        if (quantity == null || quantity <= 0.0) {
            return 0.0;
        }

        // Quantities >= 10 usually come from gram parsing such as 100 g or 250 gr.
        // Treat those entries as "per 100 g" multipliers for calorie/macro math.
        if (quantity >= 10.0) {
            return quantity / 100.0;
        }

        return quantity;
    }
}
