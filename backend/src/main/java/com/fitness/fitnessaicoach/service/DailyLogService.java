package com.fitness.fitnessaicoach.service;

import com.fitness.fitnessaicoach.domain.DailyLog;
import com.fitness.fitnessaicoach.domain.User;
import com.fitness.fitnessaicoach.dto.DailyLogRequest;
import com.fitness.fitnessaicoach.dto.DailyLogResponse;
import com.fitness.fitnessaicoach.dto.CalorieBalanceResponse;
import com.fitness.fitnessaicoach.dto.DailyLogSummaryResponseDto;
import com.fitness.fitnessaicoach.exception.DailyLogNotFoundException;
import com.fitness.fitnessaicoach.exception.UserNotFoundException;
import com.fitness.fitnessaicoach.repository.DailyLogRepository;
import com.fitness.fitnessaicoach.repository.MealItemRepository;
import com.fitness.fitnessaicoach.repository.MealRepository;
import com.fitness.fitnessaicoach.repository.UserRepository;
import com.fitness.fitnessaicoach.repository.WorkoutSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DailyLogService {

    private static final int CALORIE_SCALE = 2;
    private static final RoundingMode CALORIE_ROUNDING = RoundingMode.HALF_UP;

    private final DailyLogRepository dailyLogRepository;
    private final MealRepository mealRepository;
    private final MealItemRepository mealItemRepository;
    private final WorkoutSessionRepository workoutSessionRepository;
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

    public DailyLogSummaryResponseDto getDailyLogSummary(UUID id) {
        DailyLog dailyLog = dailyLogRepository.findById(id)
                .orElseThrow(() -> new DailyLogNotFoundException("Daily log not found."));

        long totalMeals = mealRepository.countByDailyLogId(id);
        long totalWorkoutSessions = workoutSessionRepository.countByDailyLogId(id);
        Double totalCaloriesConsumed = mealItemRepository.sumCalculatedCaloriesByDailyLogId(id);
        Double totalCaloriesBurned = workoutSessionRepository.sumCaloriesBurnedByDailyLogId(id);

        return DailyLogSummaryResponseDto.builder()
                .dailyLogId(dailyLog.getId())
                .date(dailyLog.getLogDate())
                .totalMeals(toIntSafe(totalMeals))
                .totalWorkoutSessions(toIntSafe(totalWorkoutSessions))
                .totalCaloriesConsumed(toBigDecimalOrZero(totalCaloriesConsumed))
                .totalCaloriesBurned(toBigDecimalOrZero(totalCaloriesBurned))
                .totalSteps(dailyLog.getSteps() != null ? dailyLog.getSteps() : 0)
                .build();
    }

    public CalorieBalanceResponse getDailyLogCalorieBalance(UUID id) {
        DailyLog dailyLog = dailyLogRepository.findById(id)
                .orElseThrow(() -> new DailyLogNotFoundException("Daily log not found."));

        BigDecimal caloriesConsumed = toBigDecimalOrZero(mealItemRepository.sumCalculatedCaloriesByDailyLogId(dailyLog.getId()));
        BigDecimal caloriesBurned = toBigDecimalOrZero(workoutSessionRepository.sumCaloriesBurnedByDailyLogId(dailyLog.getId()));
        BigDecimal calorieBalance = caloriesConsumed.subtract(caloriesBurned)
                .setScale(CALORIE_SCALE, CALORIE_ROUNDING);

        return CalorieBalanceResponse.builder()
                .dailyLogId(dailyLog.getId())
                .caloriesConsumed(caloriesConsumed)
                .caloriesBurned(caloriesBurned)
                .calorieBalance(calorieBalance)
                .build();
    }

    private int toIntSafe(long value) {
        if (value > Integer.MAX_VALUE) {
            throw new IllegalStateException("Daily log summary aggregate exceeds supported integer range.");
        }

        return (int) value;
    }

    private BigDecimal toBigDecimalOrZero(Double value) {
        return BigDecimal.valueOf(value != null ? value : 0)
                .setScale(CALORIE_SCALE, CALORIE_ROUNDING);
    }

    public List<DailyLogResponse> getDailyLogsByUserId(UUID userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found."));

        return dailyLogRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    public DailyLogResponse getDailyLogByUserIdAndDate(UUID userId, LocalDate date) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found."));

        DailyLog dailyLog = dailyLogRepository.findByUserIdAndLogDate(userId, date)
                .orElseThrow(() -> new DailyLogNotFoundException("Daily log not found."));

        return toResponse(dailyLog);
    }

    public DailyLogResponse getOrCreateTodayLog(UUID userId) {
        Objects.requireNonNull(userId, "userId must not be null");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found."));

        LocalDate today = LocalDate.now();

        return dailyLogRepository.findByUserIdAndLogDate(userId, today)
                .map(this::toResponse)
                .orElseGet(() -> createTodayLog(user, today));
    }

    private DailyLogResponse createTodayLog(User user, LocalDate today) {
        DailyLog dailyLog = new DailyLog();
        dailyLog.setLogDate(today);
        dailyLog.setSteps(0);
        dailyLog.setCaloriesConsumed(0.0);
        dailyLog.setCaloriesBurned(0.0);
        dailyLog.setUser(user);

        DailyLog savedDailyLog = dailyLogRepository.save(dailyLog);
        return toResponse(savedDailyLog);
    }

    private DailyLogResponse toResponse(DailyLog dailyLog) {
        return DailyLogResponse.builder()
                .id(dailyLog.getId())
                .date(dailyLog.getLogDate())
                .steps(dailyLog.getSteps())
                .caloriesConsumed(dailyLog.getCaloriesConsumed())
                .caloriesBurned(dailyLog.getCaloriesBurned())
                .userId(dailyLog.getUser() != null ? dailyLog.getUser().getId() : null)
                .build();
    }
}
