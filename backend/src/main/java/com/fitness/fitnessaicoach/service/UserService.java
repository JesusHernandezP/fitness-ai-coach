package com.fitness.fitnessaicoach.service;

import com.fitness.fitnessaicoach.domain.User;
import com.fitness.fitnessaicoach.dto.UserRequest;
import com.fitness.fitnessaicoach.dto.UserResponse;
import com.fitness.fitnessaicoach.exception.UserNotFoundException;
import com.fitness.fitnessaicoach.exception.EmailAlreadyUsedException;
import com.fitness.fitnessaicoach.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Crear usuario (registro)
    public UserResponse create(UserRequest request) {

        // Validación de email duplicado
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyUsedException("El email ya está en uso.");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .age(request.getAge())
                .heightCm(request.getHeightCm())
                .weightKg(request.getWeightKg())
                .createdAt(LocalDateTime.now())
                .build();

        User saved = userRepository.save(user);

        return toResponse(saved);
    }

    // Obtener un usuario por ID
    public UserResponse getById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado."));
        return toResponse(user);
    }

    // Listar todos los usuarios
    public java.util.List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Conversión User → UserResponse
    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .age(user.getAge())
                .heightCm(user.getHeightCm())
                .weightKg(user.getWeightKg())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
