package com.fitness.fitnessaicoach.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class BodyMetricsProgressResponse {

    private LocalDate date;

    private Double weight;
}
