package com.fitness.fitnessaicoach.service;

import com.fitness.fitnessaicoach.domain.User;
import com.fitness.fitnessaicoach.dto.LoginRequest;
import com.fitness.fitnessaicoach.dto.LoginResponse;
import com.fitness.fitnessaicoach.exception.InvalidCredentialsException;
import com.fitness.fitnessaicoach.repository.UserRepository;
import com.fitness.fitnessaicoach.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Credenciales incorrectas."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Credenciales incorrectas.");
        }
        String token = jwtService.generateToken(user.getEmail());

        return LoginResponse.builder()
                .token(token)
                .build();
    }
}
