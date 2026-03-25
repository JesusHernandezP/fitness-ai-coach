package com.fitness.fitnessaicoach.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
public class DailyLogSummaryResponseDto {

    private UUID dailyLogId;

    private LocalDate date;

    private Integer totalMeals;

    private Integer totalWorkoutSessions;

    private BigDecimal totalCaloriesConsumed;

    private BigDecimal totalCaloriesBurned;

    private Integer totalSteps;
}
