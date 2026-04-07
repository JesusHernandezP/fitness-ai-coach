package com.fitness.fitnessaicoach.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyNutritionSummaryResponse {

    private LocalDate date;
    private Double targetCalories;
    private Double targetProtein;
    private Double targetCarbs;
    private Double targetFat;
    private Double consumedCalories;
    private Double consumedProtein;
    private Double consumedCarbs;
    private Double consumedFat;
    private Double remainingCalories;
    private Double remainingProtein;
    private Double remainingCarbs;
    private Double remainingFat;
    private Integer steps;
    private Double caloriesBurned;
    private String dietType;
    private String goalType;
    private List<String> adherenceNotes;
}
