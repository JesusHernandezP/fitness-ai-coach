package com.fitness.fitnessaicoach.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class CalorieBalanceResponse {

    private UUID dailyLogId;

    private BigDecimal caloriesConsumed;

    private BigDecimal caloriesBurned;

    private BigDecimal calorieBalance;
}
