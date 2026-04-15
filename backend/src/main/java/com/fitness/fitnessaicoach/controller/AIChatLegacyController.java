package com.fitness.fitnessaicoach.controller;

import com.fitness.fitnessaicoach.dto.ai.AIChatMessageRequest;
import com.fitness.fitnessaicoach.dto.ai.AIChatMessageResponse;
import com.fitness.fitnessaicoach.service.AIChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai-chat")
@RequiredArgsConstructor
@Tag(name = "AI Chat", description = "Legacy conversational AI coaching endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AIChatLegacyController {

    private final AIChatService aiChatService;

    @PostMapping("/message")
    @Operation(summary = "Send a coaching chat message (legacy)")
    public ResponseEntity<AIChatMessageResponse> sendMessage(
            Authentication authentication,
            @Valid @RequestBody AIChatMessageRequest request
    ) {
        String reply = aiChatService.sendMessage(authentication.getName(), request.getMessage(), null);
        return ResponseEntity.ok(new AIChatMessageResponse(reply));
    }
}
