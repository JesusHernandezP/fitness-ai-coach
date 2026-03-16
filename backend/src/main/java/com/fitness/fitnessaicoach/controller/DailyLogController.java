package com.fitness.fitnessaicoach.controller;

import com.fitness.fitnessaicoach.dto.DailyLogRequest;
import com.fitness.fitnessaicoach.dto.DailyLogResponse;
import com.fitness.fitnessaicoach.dto.DailyLogSummaryResponseDto;
import com.fitness.fitnessaicoach.service.DailyLogService;
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
@RequestMapping("/api/daily-logs")
@RequiredArgsConstructor
@Tag(name = "Daily Logs", description = "Daily log management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class DailyLogController {

    private final DailyLogService dailyLogService;

    @PostMapping
    @Operation(summary = "Create a new daily log")
    public ResponseEntity<DailyLogResponse> createDailyLog(
            @Valid @RequestBody DailyLogRequest request) {

        DailyLogResponse response = dailyLogService.createDailyLog(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all daily logs")
    public List<DailyLogResponse> getAllDailyLogs() {
        return dailyLogService.getAllDailyLogs();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get daily log by id")
    public ResponseEntity<DailyLogResponse> getDailyLogById(@PathVariable UUID id) {
        DailyLogResponse response = dailyLogService.getDailyLogById(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete daily log by id")
    public ResponseEntity<Void> deleteDailyLog(@PathVariable UUID id) {
        dailyLogService.deleteDailyLog(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/summary")
    @Operation(summary = "Get daily log summary by id")
    public ResponseEntity<DailyLogSummaryResponseDto> getDailyLogSummary(@PathVariable UUID id) {
        DailyLogSummaryResponseDto response = dailyLogService.getDailyLogSummary(id);
        return ResponseEntity.ok(response);
    }
}
