package com.fitness.fitnessaicoach.controller;

<<<<<<< HEAD
import com.fitness.fitnessaicoach.dto.BodyMetricsProgressResponse;
=======
import com.fitness.fitnessaicoach.dto.ApiResponse;
>>>>>>> main
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
<<<<<<< HEAD
import org.springframework.security.core.Authentication;
=======
>>>>>>> main
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
<<<<<<< HEAD
    public ResponseEntity<BodyMetricsResponse> createBodyMetrics(
            Authentication authentication,
            @Valid @RequestBody BodyMetricsRequest request) {

        BodyMetricsResponse response = bodyMetricsService.createBodyMetrics(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
=======
    public ResponseEntity<ApiResponse<BodyMetricsResponse>> createBodyMetrics(
            @Valid @RequestBody BodyMetricsRequest request) {

        BodyMetricsResponse response = bodyMetricsService.createBodyMetrics(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(HttpStatus.CREATED.value(), response));
>>>>>>> main
    }

    @GetMapping
    @Operation(summary = "Get all body metrics")
<<<<<<< HEAD
    public List<BodyMetricsResponse> getAllBodyMetrics(Authentication authentication) {
        return bodyMetricsService.getAllBodyMetrics(authentication.getName());
    }

    @GetMapping("/progress")
    @Operation(summary = "Get body weight progress history")
    public List<BodyMetricsProgressResponse> getWeightProgress(Authentication authentication) {
        return bodyMetricsService.getWeightProgress(authentication.getName());
=======
    public ResponseEntity<ApiResponse<List<BodyMetricsResponse>>> getAllBodyMetrics() {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), bodyMetricsService.getAllBodyMetrics()));
>>>>>>> main
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get body metrics by id")
<<<<<<< HEAD
    public ResponseEntity<BodyMetricsResponse> getBodyMetricsById(
            Authentication authentication,
            @PathVariable UUID id) {

        BodyMetricsResponse response = bodyMetricsService.getBodyMetricsById(authentication.getName(), id);
        return ResponseEntity.ok(response);
=======
    public ResponseEntity<ApiResponse<BodyMetricsResponse>> getBodyMetricsById(@PathVariable UUID id) {
        BodyMetricsResponse response = bodyMetricsService.getBodyMetricsById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), response));
>>>>>>> main
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete body metrics by id")
<<<<<<< HEAD
    public ResponseEntity<Void> deleteBodyMetrics(
            Authentication authentication,
            @PathVariable UUID id) {

        bodyMetricsService.deleteBodyMetrics(authentication.getName(), id);
        return ResponseEntity.noContent().build();
=======
    public ResponseEntity<ApiResponse<Void>> deleteBodyMetrics(@PathVariable UUID id) {
        bodyMetricsService.deleteBodyMetrics(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), null));
>>>>>>> main
    }
}
