package com.fitness.fitnessaicoach.service;

<<<<<<< HEAD
import com.fitness.fitnessaicoach.domain.ActivityLevel;
import com.fitness.fitnessaicoach.domain.User;
import com.fitness.fitnessaicoach.domain.UserSex;
import com.fitness.fitnessaicoach.dto.UserProfileUpdateRequest;
import com.fitness.fitnessaicoach.dto.UserRequest;
import com.fitness.fitnessaicoach.dto.UserResponse;
import com.fitness.fitnessaicoach.exception.EmailAlreadyUsedException;
import com.fitness.fitnessaicoach.exception.UserNotFoundException;
=======
import com.fitness.fitnessaicoach.domain.User;
import com.fitness.fitnessaicoach.dto.UserRequest;
import com.fitness.fitnessaicoach.dto.UserResponse;
import com.fitness.fitnessaicoach.exception.UserNotFoundException;
import com.fitness.fitnessaicoach.exception.EmailAlreadyUsedException;
>>>>>>> main
import com.fitness.fitnessaicoach.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
<<<<<<< HEAD
import java.util.List;
=======
>>>>>>> main
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

<<<<<<< HEAD
    public UserResponse create(UserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyUsedException("El email ya esta en uso.");
=======
    // Crear usuario (registro)
    public UserResponse create(UserRequest request) {

        // Validación de email duplicado
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyUsedException("El email ya está en uso.");
>>>>>>> main
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
<<<<<<< HEAD
                .password(passwordEncoder.encode(request.getPassword()))
                .age(request.getAge())
                .heightCm(request.getHeightCm())
                .weightKg(request.getWeightKg())
                .sex(request.getSex() != null ? request.getSex() : UserSex.MALE)
                .activityLevel(request.getActivityLevel() != null ? request.getActivityLevel() : ActivityLevel.MODERATE)
=======
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .age(request.getAge())
                .heightCm(request.getHeightCm())
                .weightKg(request.getWeightKg())
>>>>>>> main
                .createdAt(LocalDateTime.now())
                .build();

        User saved = userRepository.save(user);
<<<<<<< HEAD
        return toResponse(saved);
    }

=======

        return toResponse(saved);
    }

    // Obtener un usuario por ID
>>>>>>> main
    public UserResponse getById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado."));
        return toResponse(user);
    }

<<<<<<< HEAD
    public List<UserResponse> getAllUsers() {
=======
    // Listar todos los usuarios
    public java.util.List<UserResponse> getAllUsers() {
>>>>>>> main
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

<<<<<<< HEAD
    public UserResponse updateProfile(UUID id, String email, UserProfileUpdateRequest request) {
        User user = userRepository.findByIdAndEmail(id, email)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado."));

        user.setAge(request.getAge());
        user.setHeightCm(request.getHeightCm());
        user.setSex(request.getSex());
        user.setActivityLevel(request.getActivityLevel());

        User saved = userRepository.save(user);
        return toResponse(saved);
    }

=======
    // Conversión User → UserResponse
>>>>>>> main
    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .age(user.getAge())
                .heightCm(user.getHeightCm())
                .weightKg(user.getWeightKg())
<<<<<<< HEAD
                .sex(user.getSex())
                .activityLevel(user.getActivityLevel())
=======
>>>>>>> main
                .createdAt(user.getCreatedAt())
                .build();
    }
}
