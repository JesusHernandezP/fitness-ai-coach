package com.fitness.fitnessaicoach.dto.ai;

public class AICoachingResponse {

<<<<<<< HEAD
    private String analysis;
=======
    private AIAnalysisResponse analysis;
>>>>>>> main
    private String advice;

    public AICoachingResponse() {
    }

<<<<<<< HEAD
    public AICoachingResponse(String analysis, String advice) {
=======
    public AICoachingResponse(AIAnalysisResponse analysis, String advice) {
>>>>>>> main
        this.analysis = analysis;
        this.advice = advice;
    }

<<<<<<< HEAD
    public String getAnalysis() {
        return analysis;
    }

    public void setAnalysis(String analysis) {
=======
    public AIAnalysisResponse getAnalysis() {
        return analysis;
    }

    public void setAnalysis(AIAnalysisResponse analysis) {
>>>>>>> main
        this.analysis = analysis;
    }

    public String getAdvice() {
        return advice;
    }

    public void setAdvice(String advice) {
        this.advice = advice;
    }
}
