package com.fitness.fitnessaicoach.controller;

import com.fitness.fitnessaicoach.dto.ApiResponse;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<ApiResponse<BodyMetricsResponse>> createBodyMetrics(
            Authentication authentication,
            @Valid @RequestBody BodyMetricsRequest request
    ) {
        BodyMetricsResponse response = bodyMetricsService.createBodyMetrics(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), response));
    }

    @GetMapping
    @Operation(summary = "Get all body metrics")
    public ResponseEntity<ApiResponse<List<BodyMetricsResponse>>> getAllBodyMetrics(Authentication authentication) {
        return ResponseEntity.ok(
                new ApiResponse<>(HttpStatus.OK.value(), bodyMetricsService.getAllBodyMetrics(authentication.getName()))
        );
    }

    @GetMapping("/progress")
    @Operation(summary = "Get body weight progress history")
    public ResponseEntity<ApiResponse<List<BodyMetricsProgressResponse>>> getWeightProgress(Authentication authentication) {
        return ResponseEntity.ok(
                new ApiResponse<>(HttpStatus.OK.value(), bodyMetricsService.getWeightProgress(authentication.getName()))
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get body metrics by id")
    public ResponseEntity<ApiResponse<BodyMetricsResponse>> getBodyMetricsById(
            Authentication authentication,
            @PathVariable UUID id
    ) {
        BodyMetricsResponse response = bodyMetricsService.getBodyMetricsById(authentication.getName(), id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete body metrics by id")
    public ResponseEntity<ApiResponse<Void>> deleteBodyMetrics(
            Authentication authentication,
            @PathVariable UUID id
    ) {
        bodyMetricsService.deleteBodyMetrics(authentication.getName(), id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), null));
    }
}
