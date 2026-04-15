package com.fitness.fitnessaicoach.service;

import com.fitness.fitnessaicoach.domain.ActivityLevel;
import com.fitness.fitnessaicoach.domain.User;
import com.fitness.fitnessaicoach.domain.UserSex;
import com.fitness.fitnessaicoach.dto.UserProfileUpdateRequest;
import com.fitness.fitnessaicoach.dto.UserRequest;
import com.fitness.fitnessaicoach.dto.UserResponse;
import com.fitness.fitnessaicoach.exception.EmailAlreadyUsedException;
import com.fitness.fitnessaicoach.exception.UserNotFoundException;
import com.fitness.fitnessaicoach.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse create(UserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyUsedException("El email ya esta en uso.");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .age(request.getAge())
                .heightCm(request.getHeightCm())
                .weightKg(request.getWeightKg())
                .sex(request.getSex() != null ? request.getSex() : UserSex.MALE)
                .activityLevel(request.getActivityLevel() != null ? request.getActivityLevel() : ActivityLevel.MODERATE)
                .createdAt(LocalDateTime.now())
                .build();

        return toResponse(userRepository.save(user));
    }

    public UserResponse getById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado."));
        return toResponse(user);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public UserResponse updateProfile(UUID id, String email, UserProfileUpdateRequest request) {
        User user = userRepository.findByIdAndEmail(id, email)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado."));

        if (request.getAge() != null) {
            user.setAge(request.getAge());
        }
        if (request.getHeightCm() != null) {
            user.setHeightCm(request.getHeightCm());
        }
        if (request.getWeightKg() != null) {
            user.setWeightKg(request.getWeightKg());
        }
        if (request.getSex() != null) {
            user.setSex(request.getSex());
        }
        if (request.getActivityLevel() != null) {
            user.setActivityLevel(request.getActivityLevel());
        }

        return toResponse(userRepository.save(user));
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .age(user.getAge())
                .heightCm(user.getHeightCm())
                .weightKg(user.getWeightKg())
                .sex(user.getSex())
                .activityLevel(user.getActivityLevel())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
