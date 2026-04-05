package com.fitness.fitnessaicoach.ai.provider;

public interface AITextGenerationClient {

    String generateText(String prompt);

    String getModelName();
}
