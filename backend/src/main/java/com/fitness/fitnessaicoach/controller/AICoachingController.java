package com.fitness.fitnessaicoach.controller;

import com.fitness.fitnessaicoach.dto.ai.AICoachingResponse;
import com.fitness.fitnessaicoach.dto.ai.AIWeeklySummaryResponse;
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
import org.springframework.security.core.Authentication;

import java.util.UUID;

@RestController
@RequestMapping("/api/ai-coach")
@RequiredArgsConstructor
@Tag(name = "AI Coaching", description = "AI coaching endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AICoachingController {

    private final AICoachingService aiCoachingService;

    @GetMapping("/daily-log/{dailyLogId}")
    @Operation(summary = "Get AI coaching contract for a daily log")
    public ResponseEntity<AICoachingResponse> getDailyLogCoaching(@PathVariable UUID dailyLogId) {
        return ResponseEntity.ok(aiCoachingService.getCoaching(dailyLogId));
    }

    @GetMapping("/weekly-summary")
    @Operation(summary = "Get weekly AI coaching summary for the authenticated user")
    public ResponseEntity<AIWeeklySummaryResponse> getWeeklySummary(Authentication authentication) {
        return ResponseEntity.ok(aiCoachingService.getWeeklySummary(authentication.getName()));
    }
}
