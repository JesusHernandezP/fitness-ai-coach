package com.fitness.fitnessaicoach.dto.ai;

import com.fitness.fitnessaicoach.dto.DailyNutritionSummaryResponse;

import java.util.List;

public class AIChatMessageResponse {

    private String reply;
    private List<String> loggedSummary;
    private DailyNutritionSummaryResponse dailySummary;

    public AIChatMessageResponse() {
    }

    public AIChatMessageResponse(String reply) {
        this.reply = reply;
    }

    public AIChatMessageResponse(String reply, List<String> loggedSummary, DailyNutritionSummaryResponse dailySummary) {
        this.reply = reply;
        this.loggedSummary = loggedSummary;
        this.dailySummary = dailySummary;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public List<String> getLoggedSummary() {
        return loggedSummary;
    }

    public void setLoggedSummary(List<String> loggedSummary) {
        this.loggedSummary = loggedSummary;
    }

    public DailyNutritionSummaryResponse getDailySummary() {
        return dailySummary;
    }

    public void setDailySummary(DailyNutritionSummaryResponse dailySummary) {
        this.dailySummary = dailySummary;
    }
}
