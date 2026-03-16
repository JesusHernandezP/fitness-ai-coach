package com.fitness.fitnessaicoach.controller;

import com.fitness.fitnessaicoach.dto.ai.AIAnalysisResponse;
import com.fitness.fitnessaicoach.service.AIAnalysisService;
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
@RequestMapping("/api/ai-analysis")
@RequiredArgsConstructor
@Tag(name = "AI Analysis", description = "AI analysis preparation endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AIAnalysisController {

    private final AIAnalysisService aiAnalysisService;

    @GetMapping("/daily-log/{dailyLogId}")
    @Operation(summary = "Build AI analysis payload for a daily log")
    public ResponseEntity<AIAnalysisResponse> getDailyLogAiAnalysis(@PathVariable UUID dailyLogId) {
        AIAnalysisResponse response = aiAnalysisService.getDailyLogAiAnalysis(dailyLogId);
        return ResponseEntity.ok(response);
    }
}
