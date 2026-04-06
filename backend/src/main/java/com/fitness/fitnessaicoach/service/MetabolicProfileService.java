package com.fitness.fitnessaicoach.service;

import com.fitness.fitnessaicoach.domain.Goal;
import com.fitness.fitnessaicoach.domain.User;
import com.fitness.fitnessaicoach.dto.MetabolicProfileRequest;
import com.fitness.fitnessaicoach.dto.MetabolicProfileResponse;
import com.fitness.fitnessaicoach.exception.UserNotFoundException;
import com.fitness.fitnessaicoach.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MetabolicProfileService {

    private final UserRepository userRepository;
    private final GoalService goalService;

    public MetabolicProfileResponse saveProfile(String email, MetabolicProfileRequest request) {
        User user = findUserByEmail(email);
        applyProfile(user, request);
        User savedUser = userRepository.save(user);
        Goal goal = syncGoal(savedUser, request);
        return toResponse(savedUser, goal);
    }

    public MetabolicProfileResponse getProfile(String email) {
        User user = findUserByEmail(email);
        Goal latestGoal = goalService.getLatestGoalForUser(user.getId());
        return toResponse(user, latestGoal);
    }

    public MetabolicProfileResponse updateProfile(String email, MetabolicProfileRequest request) {
        User user = findUserByEmail(email);
        applyProfile(user, request);
        User savedUser = userRepository.save(user);
        Goal goal = syncGoal(savedUser, request);
        return toResponse(savedUser, goal);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found."));
    }

    private Goal syncGoal(User user, MetabolicProfileRequest request) {
        if (request.getGoalType() == null) {
            return null;
        }
        return goalService.upsertGoalFromProfile(user, request.getGoalType());
    }

    private void applyProfile(User user, MetabolicProfileRequest request) {
        user.setAge(request.getAge());
        user.setHeightCm(request.getHeightCm());
        user.setSex(request.getSex());
        user.setActivityLevel(request.getActivityLevel());
    }

    private MetabolicProfileResponse toResponse(User user, Goal goal) {
        return MetabolicProfileResponse.builder()
                .userId(user.getId())
                .age(user.getAge())
                .heightCm(user.getHeightCm())
                .sex(user.getSex())
                .activityLevel(user.getActivityLevel())
                .goalType(goal != null ? goal.getGoalType() : null)
                .targetCalories(goal != null ? goal.getTargetCalories() : null)
                .targetProtein(goal != null ? goal.getTargetProtein() : null)
                .targetCarbs(goal != null ? goal.getTargetCarbs() : null)
                .targetFat(goal != null ? goal.getTargetFat() : null)
                .build();
    }
}
