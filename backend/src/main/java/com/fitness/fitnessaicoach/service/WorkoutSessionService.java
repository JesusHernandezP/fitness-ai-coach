package com.fitness.fitnessaicoach.service;

import com.fitness.fitnessaicoach.domain.DailyLog;
import com.fitness.fitnessaicoach.domain.Exercise;
import com.fitness.fitnessaicoach.domain.WorkoutSession;
import com.fitness.fitnessaicoach.dto.WorkoutSessionRequest;
import com.fitness.fitnessaicoach.dto.WorkoutSessionResponse;
import com.fitness.fitnessaicoach.exception.ExerciseNotFoundException;
import com.fitness.fitnessaicoach.exception.DailyLogNotFoundException;
import com.fitness.fitnessaicoach.exception.WorkoutSessionNotFoundException;
import com.fitness.fitnessaicoach.repository.DailyLogRepository;
import com.fitness.fitnessaicoach.repository.ExerciseRepository;
import com.fitness.fitnessaicoach.repository.WorkoutSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkoutSessionService {

    private final WorkoutSessionRepository workoutSessionRepository;
    private final DailyLogRepository dailyLogRepository;
    private final ExerciseRepository exerciseRepository;

    @Transactional
    public WorkoutSessionResponse createWorkoutSession(WorkoutSessionRequest request) {
        DailyLog dailyLog = dailyLogRepository.findById(request.getDailyLogId())
                .orElseThrow(() -> new DailyLogNotFoundException("Daily log not found."));

        Exercise exercise = exerciseRepository.findById(request.getExerciseId())
                .orElseThrow(() -> new ExerciseNotFoundException("Exercise not found."));

        WorkoutSession workoutSession = WorkoutSession.builder()
                .dailyLog(dailyLog)
                .exercise(exercise)
                .sets(request.getSets())
                .reps(request.getReps())
                .duration(request.getDuration())
                .caloriesBurned(request.getCaloriesBurned())
                .build();

        WorkoutSession saved = workoutSessionRepository.save(workoutSession);
        syncDailyLogCaloriesBurned(dailyLog.getId());

        return toResponse(saved);
    }

    public List<WorkoutSessionResponse> getAllWorkoutSessions() {
        return workoutSessionRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public WorkoutSessionResponse getWorkoutSessionById(UUID id) {
        WorkoutSession workoutSession = workoutSessionRepository.findById(id)
                .orElseThrow(() -> new WorkoutSessionNotFoundException("Workout session not found."));

        return toResponse(workoutSession);
    }

    @Transactional
    public void deleteWorkoutSession(UUID id) {
        WorkoutSession workoutSession = workoutSessionRepository.findById(id)
                .orElseThrow(() -> new WorkoutSessionNotFoundException("Workout session not found."));

        UUID dailyLogId = workoutSession.getDailyLog().getId();
        workoutSessionRepository.delete(workoutSession);
        syncDailyLogCaloriesBurned(dailyLogId);
    }

    private WorkoutSessionResponse toResponse(WorkoutSession workoutSession) {
        return WorkoutSessionResponse.builder()
                .id(workoutSession.getId())
                .dailyLogId(workoutSession.getDailyLog() != null ? workoutSession.getDailyLog().getId() : null)
                .exerciseId(workoutSession.getExercise() != null ? workoutSession.getExercise().getId() : null)
                .sets(workoutSession.getSets())
                .reps(workoutSession.getReps())
                .duration(workoutSession.getDuration())
                .caloriesBurned(workoutSession.getCaloriesBurned())
                .build();
    }

    private void syncDailyLogCaloriesBurned(UUID dailyLogId) {
        dailyLogRepository.findById(dailyLogId).ifPresent(dailyLog -> {
            dailyLog.setCaloriesBurned(Objects.requireNonNullElse(
                    workoutSessionRepository.sumCaloriesBurnedByDailyLogId(dailyLogId),
                    0.0
            ));
            dailyLogRepository.save(dailyLog);
        });
    }
}
