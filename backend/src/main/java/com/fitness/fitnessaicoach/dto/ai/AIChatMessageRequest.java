package com.fitness.fitnessaicoach.dto.ai;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AIChatMessageRequest {

    @NotBlank(message = "Message must not be blank.")
    private String message;
}
