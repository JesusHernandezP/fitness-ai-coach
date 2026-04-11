package com.fitness.fitnessaicoach.service;

import com.fitness.fitnessaicoach.domain.ActivityLevel;
import com.fitness.fitnessaicoach.domain.Goal;
import com.fitness.fitnessaicoach.domain.User;
import com.fitness.fitnessaicoach.domain.UserGoalType;
import com.fitness.fitnessaicoach.domain.UserSex;
import com.fitness.fitnessaicoach.dto.GoalRequest;
import com.fitness.fitnessaicoach.dto.GoalResponse;
import com.fitness.fitnessaicoach.exception.GoalAlreadyExistsException;
import com.fitness.fitnessaicoach.exception.GoalNotFoundException;
import com.fitness.fitnessaicoach.exception.UserNotFoundException;
import com.fitness.fitnessaicoach.repository.GoalRepository;
import com.fitness.fitnessaicoach.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepository;
    private final UserRepository userRepository;

    public GoalResponse createGoal(String email, GoalRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found."));

        ensureGoalNotAlreadyCreatedToday(user.getId());
        MacroTargets macroTargets = resolveMacroTargets(user, request);

        Goal goal = Goal.builder()
                .goalType(request.getGoalType())
                .targetWeight(request.getTargetWeight())
                .targetCalories(macroTargets.targetCalories())
                .targetProtein(macroTargets.targetProtein())
                .targetCarbs(macroTargets.targetCarbs())
                .targetFat(macroTargets.targetFat())
                .user(user)
                .build();

        return toResponse(goalRepository.save(goal));
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
                .targetProtein(goal.getTargetProtein())
                .targetCarbs(goal.getTargetCarbs())
                .targetFat(goal.getTargetFat())
                .userId(goal.getUser() != null ? goal.getUser().getId() : null)
                .build();
    }

    private void ensureGoalNotAlreadyCreatedToday(UUID userId) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime startOfNextDay = today.plusDays(1).atStartOfDay();

        if (goalRepository.existsByUserIdAndCreatedAtBetween(userId, startOfDay, startOfNextDay)) {
            throw new GoalAlreadyExistsException("You already set your goal today");
        }
    }

    private MacroTargets resolveMacroTargets(User user, GoalRequest request) {
        if (hasProfileForMacroCalculation(user)) {
            return calculateMacroTargets(user, request.getGoalType());
        }

        if (request.getTargetCalories() == null) {
            throw new IllegalStateException(
                    "User profile is missing weight, height, or age required to calculate target calories."
            );
        }

        return new MacroTargets(roundToScale(request.getTargetCalories()), null, null, null);
    }

    private boolean hasProfileForMacroCalculation(User user) {
        return user.getWeightKg() != null && user.getHeightCm() != null && user.getAge() != null;
    }

    private MacroTargets calculateMacroTargets(User user, UserGoalType goalType) {
        UserSex sex = user.getSex() != null ? user.getSex() : UserSex.MALE;
        ActivityLevel activityLevel = user.getActivityLevel() != null ? user.getActivityLevel() : ActivityLevel.MODERATE;
        double sexAdjustment = sex == UserSex.MALE ? 5.0 : -161.0;
        double bmr = (10 * user.getWeightKg())
                + (6.25 * user.getHeightCm())
                - (5 * user.getAge())
                + sexAdjustment;
        double maintenanceCalories = bmr * resolveActivityMultiplier(activityLevel);

        double adjustedCalories = switch (goalType) {
            case LOSE_WEIGHT -> maintenanceCalories - 300;
            case BUILD_MUSCLE -> maintenanceCalories + 300;
            case MAINTAIN -> maintenanceCalories;
        };

        double proteinMultiplier = switch (goalType) {
            case LOSE_WEIGHT -> 2.0;
            case BUILD_MUSCLE -> 2.2;
            case MAINTAIN -> 1.6;
        };

        double targetProtein = roundToScale(user.getWeightKg() * proteinMultiplier);
        double targetFat = roundToScale(user.getWeightKg() * 0.8);
        double carbCalories = adjustedCalories - ((targetProtein * 4) + (targetFat * 9));
        double targetCarbs = roundToScale(Math.max(0.0, carbCalories / 4.0));

        return new MacroTargets(
                roundToScale(adjustedCalories),
                targetProtein,
                targetCarbs,
                targetFat
        );
    }

    private double resolveActivityMultiplier(ActivityLevel activityLevel) {
        return switch (activityLevel) {
            case SEDENTARY -> 1.2;
            case LIGHT -> 1.375;
            case MODERATE -> 1.55;
            case ACTIVE -> 1.725;
            case VERY_ACTIVE -> 1.9;
        };
    }

    private double roundToScale(double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private record MacroTargets(
            Double targetCalories,
            Double targetProtein,
            Double targetCarbs,
            Double targetFat
    ) {
    }
}
