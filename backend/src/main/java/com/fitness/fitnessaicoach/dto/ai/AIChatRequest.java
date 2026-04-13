package com.fitness.fitnessaicoach.dto.ai;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AIChatRequest {

    @NotBlank(message = "Message is required.")
    private String message;

    private String conversationId;
}
