package com.fitness.fitnessaicoach.controller;

import com.fitness.fitnessaicoach.dto.ApiResponse;
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
    public ResponseEntity<ApiResponse<BodyMetricsResponse>> createBodyMetrics(
            @Valid @RequestBody BodyMetricsRequest request) {

        BodyMetricsResponse response = bodyMetricsService.createBodyMetrics(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(HttpStatus.CREATED.value(), response));
    }

    @GetMapping
    @Operation(summary = "Get all body metrics")
    public ResponseEntity<ApiResponse<List<BodyMetricsResponse>>> getAllBodyMetrics() {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), bodyMetricsService.getAllBodyMetrics()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get body metrics by id")
    public ResponseEntity<ApiResponse<BodyMetricsResponse>> getBodyMetricsById(@PathVariable UUID id) {
        BodyMetricsResponse response = bodyMetricsService.getBodyMetricsById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete body metrics by id")
    public ResponseEntity<ApiResponse<Void>> deleteBodyMetrics(@PathVariable UUID id) {
        bodyMetricsService.deleteBodyMetrics(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), null));
    }
}
