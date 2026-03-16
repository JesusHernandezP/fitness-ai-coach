package com.fitness.fitnessaicoach.exception;

public class WorkoutSessionNotFoundException extends RuntimeException {

    public WorkoutSessionNotFoundException(String message) {
        super(message);
    }
}
