package com.fitness.fitnessaicoach.controller;

import com.fitness.fitnessaicoach.dto.LoginRequest;
import com.fitness.fitnessaicoach.dto.LoginResponse;
import com.fitness.fitnessaicoach.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}