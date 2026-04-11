package com.fitness.fitnessaicoach.dto.ai;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class AIMealSummaryResponse {

    private String mealType;

    private Integer totalItems;

    private BigDecimal totalCalories;
}
