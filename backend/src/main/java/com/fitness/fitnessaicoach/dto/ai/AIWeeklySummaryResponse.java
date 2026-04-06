package com.fitness.fitnessaicoach.dto.ai;

import java.time.LocalDate;

public class AIWeeklySummaryResponse {

    private LocalDate weekStart;
    private LocalDate weekEnd;
    private String summary;
    private String recommendation;

    public AIWeeklySummaryResponse() {
    }

    public AIWeeklySummaryResponse(LocalDate weekStart, LocalDate weekEnd, String summary, String recommendation) {
        this.weekStart = weekStart;
        this.weekEnd = weekEnd;
        this.summary = summary;
        this.recommendation = recommendation;
    }

    public LocalDate getWeekStart() {
        return weekStart;
    }

    public void setWeekStart(LocalDate weekStart) {
        this.weekStart = weekStart;
    }

    public LocalDate getWeekEnd() {
        return weekEnd;
    }

    public void setWeekEnd(LocalDate weekEnd) {
        this.weekEnd = weekEnd;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }
}
