package com.fitness.fitnessaicoach.controller;

import com.fitness.fitnessaicoach.dto.BodyMetricsProgressResponse;
import com.fitness.fitnessaicoach.dto.BodyMetricsRequest;
import com.fitness.fitnessaicoach.dto.BodyMetricsResponse;
import com.fitness.fitnessaicoach.service.BodyMetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/body-metrics")
@RequiredArgsConstructor
@Tag(name = "Body Metrics", description = "Body metrics management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class BodyMetricsController {

    private final BodyMetricsService bodyMetricsService;

    @PostMapping
    @Operation(summary = "Create a new body metrics record")
    public ResponseEntity<BodyMetricsResponse> createBodyMetrics(
            Authentication authentication,
            @Valid @RequestBody BodyMetricsRequest request) {

        BodyMetricsResponse response = bodyMetricsService.createBodyMetrics(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all body metrics")
    public List<BodyMetricsResponse> getAllBodyMetrics(Authentication authentication) {
        return bodyMetricsService.getAllBodyMetrics(authentication.getName());
    }

    @GetMapping("/progress")
    @Operation(summary = "Get body weight progress history")
    public List<BodyMetricsProgressResponse> getWeightProgress(Authentication authentication) {
        return bodyMetricsService.getWeightProgress(authentication.getName());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get body metrics by id")
    public ResponseEntity<BodyMetricsResponse> getBodyMetricsById(
            Authentication authentication,
            @PathVariable UUID id) {

        BodyMetricsResponse response = bodyMetricsService.getBodyMetricsById(authentication.getName(), id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete body metrics by id")
    public ResponseEntity<Void> deleteBodyMetrics(
            Authentication authentication,
            @PathVariable UUID id) {

        bodyMetricsService.deleteBodyMetrics(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
