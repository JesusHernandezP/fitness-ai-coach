package com.fitness.fitnessaicoach.dto;

import java.time.Instant;

public class ApiResponse<T> {
    private final Instant timestamp;
    private final int status;
    private final T data;

    public ApiResponse(int status, T data) {
        this.timestamp = Instant.now();
        this.status = status;
        this.data = data;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public T getData() {
        return data;
    }
}
