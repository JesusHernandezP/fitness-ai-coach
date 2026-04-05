package com.fitness.fitnessaicoach.service;

import com.fitness.fitnessaicoach.domain.Goal;
import com.fitness.fitnessaicoach.domain.User;
import com.fitness.fitnessaicoach.domain.UserGoalType;
import com.fitness.fitnessaicoach.dto.GoalRequest;
import com.fitness.fitnessaicoach.dto.GoalResponse;
import com.fitness.fitnessaicoach.exception.GoalNotFoundException;
import com.fitness.fitnessaicoach.exception.UserNotFoundException;
import com.fitness.fitnessaicoach.repository.GoalRepository;
import com.fitness.fitnessaicoach.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoalService {

    private static final double MODERATE_ACTIVITY_MULTIPLIER = 1.55;
    private static final double DEFAULT_SEX_ADJUSTMENT = 5.0;

    private final GoalRepository goalRepository;
    private final UserRepository userRepository;

    public GoalResponse createGoal(String email, GoalRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found."));

        Goal goal = Goal.builder()
                .goalType(request.getGoalType())
                .targetWeight(request.getTargetWeight())
                .targetCalories(calculateTargetCalories(user, request.getGoalType()))
                .user(user)
                .build();

        Goal saved = goalRepository.save(goal);
        return toResponse(saved);
    }

    public List<GoalResponse> getAllGoals(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found."));

        return goalRepository.findAllByUserIdOrderByCreatedAtDescIdDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public GoalResponse getGoalById(String email, UUID id) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found."));

        Goal goal = goalRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new GoalNotFoundException("Goal not found."));

        return toResponse(goal);
    }

    public void deleteGoal(String email, UUID id) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found."));

        Goal goal = goalRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new GoalNotFoundException("Goal not found."));

        goalRepository.delete(goal);
    }

    private GoalResponse toResponse(Goal goal) {
        return GoalResponse.builder()
                .id(goal.getId())
                .goalType(goal.getGoalType())
                .targetWeight(goal.getTargetWeight())
                .targetCalories(goal.getTargetCalories())
                .userId(goal.getUser() != null ? goal.getUser().getId() : null)
                .build();
    }

    private double calculateTargetCalories(User user, UserGoalType goalType) {
        validateUserForTargetCalories(user);

        double bmr = (10 * user.getWeightKg())
                + (6.25 * user.getHeightCm())
                - (5 * user.getAge())
                + DEFAULT_SEX_ADJUSTMENT;
        double maintenanceCalories = bmr * MODERATE_ACTIVITY_MULTIPLIER;

        double adjustedCalories = switch (goalType) {
            case LOSE_WEIGHT -> maintenanceCalories - 300;
            case BUILD_MUSCLE -> maintenanceCalories + 300;
            case MAINTAIN -> maintenanceCalories;
        };

        return BigDecimal.valueOf(adjustedCalories)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private void validateUserForTargetCalories(User user) {
        if (user.getWeightKg() == null || user.getHeightCm() == null || user.getAge() == null) {
            throw new IllegalStateException("User profile is missing weight, height, or age required to calculate target calories.");
        }
    }
}
