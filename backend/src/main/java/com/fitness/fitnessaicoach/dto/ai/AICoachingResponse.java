package com.fitness.fitnessaicoach.dto.ai;

public class AICoachingResponse {

    private String analysis;
    private String advice;

    public AICoachingResponse() {
    }

    public AICoachingResponse(String analysis, String advice) {
        this.analysis = analysis;
        this.advice = advice;
    }

    public String getAnalysis() {
        return analysis;
    }

    public void setAnalysis(String analysis) {
        this.analysis = analysis;
    }

    public String getAdvice() {
        return advice;
    }

    public void setAdvice(String advice) {
        this.advice = advice;
    }
}
