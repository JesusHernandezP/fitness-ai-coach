package com.fitness.fitnessaicoach.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fitness.fitnessaicoach.dto.ApiResponse;
import com.fitness.fitnessaicoach.dto.CalorieBalanceResponse;
import com.fitness.fitnessaicoach.dto.DailyLogRequest;
import com.fitness.fitnessaicoach.dto.DailyLogResponse;
import com.fitness.fitnessaicoach.dto.DailyLogSummaryResponseDto;
import com.fitness.fitnessaicoach.dto.WeightUpdateRequest;
import com.fitness.fitnessaicoach.service.DailyLogService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/daily-logs")
@RequiredArgsConstructor
@Tag(name = "Daily Logs", description = "Daily log management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class DailyLogController {

    private final DailyLogService dailyLogService;

    @PostMapping
    @Operation(summary = "Create a new daily log")
    public ResponseEntity<ApiResponse<DailyLogResponse>> createDailyLog(
            @Valid @RequestBody DailyLogRequest request
    ) {
        DailyLogResponse response = dailyLogService.createDailyLog(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), response));
    }

    @GetMapping
    @Operation(summary = "Get all daily logs")
    public ResponseEntity<ApiResponse<List<DailyLogResponse>>> getAllDailyLogs() {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), dailyLogService.getAllDailyLogs()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get daily log by id")
    public ResponseEntity<ApiResponse<DailyLogResponse>> getDailyLogById(@PathVariable UUID id) {
        DailyLogResponse response = dailyLogService.getDailyLogById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete daily log by id")
    public ResponseEntity<ApiResponse<Void>> deleteDailyLog(@PathVariable UUID id) {
        dailyLogService.deleteDailyLog(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), null));
    }

    @GetMapping("/{id}/summary")
    @Operation(summary = "Get daily log summary by id")
    public ResponseEntity<ApiResponse<DailyLogSummaryResponseDto>> getDailyLogSummary(@PathVariable UUID id) {
        DailyLogSummaryResponseDto response = dailyLogService.getDailyLogSummary(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), response));
    }

    @GetMapping("/{id}/calorie-balance")
    @Operation(summary = "Get calorie balance for a daily log")
    public ResponseEntity<ApiResponse<CalorieBalanceResponse>> getDailyLogCalorieBalance(@PathVariable UUID id) {
        CalorieBalanceResponse response = dailyLogService.getDailyLogCalorieBalance(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), response));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get daily logs by user id")
    public ResponseEntity<ApiResponse<List<DailyLogResponse>>> getDailyLogsByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), dailyLogService.getDailyLogsByUserId(userId)));
    }

    @GetMapping("/user/{userId}/date/{date}")
    @Operation(summary = "Get daily log for a user by date")
    public ResponseEntity<ApiResponse<DailyLogResponse>> getDailyLogByUserIdAndDate(
            @PathVariable UUID userId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        DailyLogResponse response = dailyLogService.getDailyLogByUserIdAndDate(userId, date);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), response));
    }

    @GetMapping("/user/{userId}/today")
    @Operation(summary = "Get or create today's daily log for a user")
    public ResponseEntity<ApiResponse<DailyLogResponse>> getOrCreateTodayLog(@PathVariable UUID userId) {
        DailyLogResponse response = dailyLogService.getOrCreateTodayLog(userId);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), response));
    }

    @GetMapping("/today")
    @Operation(summary = "Get or create today's daily log for the authenticated user")
    public ResponseEntity<ApiResponse<DailyLogResponse>> getOrCreateTodayLog(Authentication authentication) {
        DailyLogResponse response = dailyLogService.getOrCreateTodayLogForAuthenticatedUser(authentication.getName());
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), response));
    }

    @PutMapping("/today/weight")
    @Operation(summary = "Update weight for today's daily log")
    public ResponseEntity<ApiResponse<DailyLogResponse>> updateTodayWeight(
            @Valid @RequestBody WeightUpdateRequest request,
            Authentication authentication
    ) {
        DailyLogResponse response = dailyLogService.updateTodayWeight(authentication.getName(), request.getWeightKg());
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), response));
    }
}
