package com.fitness.fitnessaicoach.service;

import com.fitness.fitnessaicoach.domain.DailyLog;
import com.fitness.fitnessaicoach.domain.User;
import com.fitness.fitnessaicoach.dto.DailyLogRequest;
import com.fitness.fitnessaicoach.dto.DailyLogResponse;
import com.fitness.fitnessaicoach.exception.DailyLogNotFoundException;
import com.fitness.fitnessaicoach.exception.UserNotFoundException;
import com.fitness.fitnessaicoach.repository.DailyLogRepository;
import com.fitness.fitnessaicoach.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DailyLogService {

    private final DailyLogRepository dailyLogRepository;
    private final UserRepository userRepository;

    public DailyLogResponse createDailyLog(DailyLogRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado."));

        DailyLog dailyLog = new DailyLog();
        dailyLog.setLogDate(request.getLogDate());
        dailyLog.setSteps(request.getSteps());
        dailyLog.setCaloriesConsumed(request.getCaloriesConsumed());
        dailyLog.setCaloriesBurned(request.getCaloriesBurned());
        dailyLog.setUser(user);

        DailyLog saved = dailyLogRepository.save(dailyLog);

        return toResponse(saved);
    }

    public List<DailyLogResponse> getAllDailyLogs() {
        return dailyLogRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public DailyLogResponse getDailyLogById(UUID id) {
        DailyLog dailyLog = dailyLogRepository.findById(id)
                .orElseThrow(() -> new DailyLogNotFoundException("Daily log not found."));

        return toResponse(dailyLog);
    }

    public void deleteDailyLog(UUID id) {
        DailyLog dailyLog = dailyLogRepository.findById(id)
                .orElseThrow(() -> new DailyLogNotFoundException("Daily log not found."));

        dailyLogRepository.delete(dailyLog);
    }

    private DailyLogResponse toResponse(DailyLog dailyLog) {
        return DailyLogResponse.builder()
                .id(dailyLog.getId())
                .logDate(dailyLog.getLogDate())
                .steps(dailyLog.getSteps())
                .caloriesConsumed(dailyLog.getCaloriesConsumed())
                .caloriesBurned(dailyLog.getCaloriesBurned())
                .userId(dailyLog.getUser() != null ? dailyLog.getUser().getId() : null)
                .build();
    }
}
