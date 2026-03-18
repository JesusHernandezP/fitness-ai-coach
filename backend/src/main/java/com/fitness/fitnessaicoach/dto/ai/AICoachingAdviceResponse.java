package com.fitness.fitnessaicoach.dto.ai;

import java.util.UUID;

public class AICoachingAdviceResponse {

    private UUID dailyLogId;

    private String advice;

    public AICoachingAdviceResponse() {
    }

    public AICoachingAdviceResponse(UUID dailyLogId, String advice) {
        this.dailyLogId = dailyLogId;
        this.advice = advice;
    }

    public UUID getDailyLogId() {
        return dailyLogId;
    }

    public void setDailyLogId(UUID dailyLogId) {
        this.dailyLogId = dailyLogId;
    }

    public String getAdvice() {
        return advice;
    }

    public void setAdvice(String advice) {
        this.advice = advice;
    }
}
