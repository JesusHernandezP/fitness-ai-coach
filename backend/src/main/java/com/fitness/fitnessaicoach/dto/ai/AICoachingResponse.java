package com.fitness.fitnessaicoach.dto.ai;

public class AICoachingResponse {

    private AIAnalysisResponse analysis;
    private String advice;

    public AICoachingResponse() {
    }

    public AICoachingResponse(AIAnalysisResponse analysis, String advice) {
        this.analysis = analysis;
        this.advice = advice;
    }

    public AIAnalysisResponse getAnalysis() {
        return analysis;
    }

    public void setAnalysis(AIAnalysisResponse analysis) {
        this.analysis = analysis;
    }

    public String getAdvice() {
        return advice;
    }

    public void setAdvice(String advice) {
        this.advice = advice;
    }
}
