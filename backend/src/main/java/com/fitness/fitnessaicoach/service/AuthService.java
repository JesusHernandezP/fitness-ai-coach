package com.fitness.fitnessaicoach.service;

import com.fitness.fitnessaicoach.domain.User;
import com.fitness.fitnessaicoach.dto.LoginRequest;
import com.fitness.fitnessaicoach.dto.LoginResponse;
import com.fitness.fitnessaicoach.exception.InvalidCredentialsException;
import com.fitness.fitnessaicoach.repository.UserRepository;
import com.fitness.fitnessaicoach.security.JwtService;
import com.fitness.fitnessaicoach.security.LogSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) {
        String sanitizedEmail = LogSanitizer.sanitizeEmail(request.getEmail());
        log.info("User {} attempted login", sanitizedEmail);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Failed login attempt for {}", sanitizedEmail);
                    return new InvalidCredentialsException("Credenciales incorrectas.");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Failed login attempt for {}", sanitizedEmail);
            throw new InvalidCredentialsException("Credenciales incorrectas.");
        }

        log.info("Successful login for {}", sanitizedEmail);
        return LoginResponse.builder()
                .token(jwtService.generateToken(user.getEmail()))
                .build();
    }
}
