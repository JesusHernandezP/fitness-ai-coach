package com.fitness.fitnessaicoach.exception;

public class FoodNotFoundException extends RuntimeException {

    public FoodNotFoundException(String message) {
        super(message);
    }
}
