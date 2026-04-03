package com.fitness.fitnessaicoach.service;

import com.fitness.fitnessaicoach.domain.Goal;
import com.fitness.fitnessaicoach.domain.User;
import com.fitness.fitnessaicoach.dto.GoalRequest;
import com.fitness.fitnessaicoach.dto.GoalResponse;
import com.fitness.fitnessaicoach.exception.GoalNotFoundException;
import com.fitness.fitnessaicoach.exception.UserNotFoundException;
import com.fitness.fitnessaicoach.repository.GoalRepository;
import com.fitness.fitnessaicoach.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

        Goal goal = Goal.builder()
                .goalType(request.getGoalType())
                .targetWeight(request.getTargetWeight())
                .targetCalories(request.getTargetCalories())
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
}
