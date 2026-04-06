package com.fitness.fitnessaicoach.service;

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

    public MetabolicProfileResponse saveProfile(String email, MetabolicProfileRequest request) {
        User user = findUserByEmail(email);
        applyProfile(user, request);
        return toResponse(userRepository.save(user));
    }

    public MetabolicProfileResponse getProfile(String email) {
        return toResponse(findUserByEmail(email));
    }

    public MetabolicProfileResponse updateProfile(String email, MetabolicProfileRequest request) {
        User user = findUserByEmail(email);
        applyProfile(user, request);
        return toResponse(userRepository.save(user));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found."));
    }

    private void applyProfile(User user, MetabolicProfileRequest request) {
        user.setAge(request.getAge());
        user.setHeightCm(request.getHeightCm());
        user.setSex(request.getSex());
        user.setActivityLevel(request.getActivityLevel());
    }

    private MetabolicProfileResponse toResponse(User user) {
        return MetabolicProfileResponse.builder()
                .userId(user.getId())
                .age(user.getAge())
                .heightCm(user.getHeightCm())
                .sex(user.getSex())
                .activityLevel(user.getActivityLevel())
                .build();
    }
}
