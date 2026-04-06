package com.fitness.fitnessaicoach.dto.ai;

import java.time.LocalDateTime;

public class AIChatMessageDto {

    private String role;
    private String message;
    private LocalDateTime createdAt;

    public AIChatMessageDto() {
    }

    public AIChatMessageDto(String role, String message, LocalDateTime createdAt) {
        this.role = role;
        this.message = message;
        this.createdAt = createdAt;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
