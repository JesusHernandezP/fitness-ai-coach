package com.fitness.fitnessaicoach.controller;

import com.fitness.fitnessaicoach.dto.ai.AIChatRequest;
import com.fitness.fitnessaicoach.dto.ai.AIChatResponse;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/ai/chat")
@RequiredArgsConstructor
@Tag(name = "AI Chat", description = "Conversational AI coaching endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AIChatController {

    private final AIChatService aiChatService;

    @PostMapping
    @Operation(summary = "Send a coaching chat message")
    public ResponseEntity<AIChatResponse> sendMessage(
            Authentication authentication,
            @Valid @RequestBody AIChatRequest request
    ) {
        UUID conversationId = parseConversationId(request.getConversationId());
        String reply = aiChatService.sendMessage(authentication.getName(), request.getMessage(), conversationId);
        return ResponseEntity.ok(new AIChatResponse(reply));
    }

    private UUID parseConversationId(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
