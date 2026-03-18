package com.fitness.fitnessaicoach.controller;

import com.fitness.fitnessaicoach.dto.ai.AICoachingResponse;
import com.fitness.fitnessaicoach.service.AICoachingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/ai-coach")
@RequiredArgsConstructor
@Tag(name = "AI Coaching", description = "AI coaching endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AICoachingController {

    private final AICoachingService aiCoachingService;

    @GetMapping("/daily-log/{dailyLogId}")
    @Operation(summary = "Get analysis and AI coaching advice for a daily log")
    public ResponseEntity<AICoachingResponse> generateCoaching(@PathVariable UUID dailyLogId) {
        return ResponseEntity.ok(aiCoachingService.getCoaching(dailyLogId));
    }
}
