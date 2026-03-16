package com.fitness.fitnessaicoach.exception;

public class MealNotFoundException extends RuntimeException {

    public MealNotFoundException(String message) {
        super(message);
    }
}
