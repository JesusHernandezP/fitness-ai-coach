package com.fitness.fitnessaicoach.controller;

import com.fitness.fitnessaicoach.dto.LoginRequest;
import com.fitness.fitnessaicoach.dto.LoginResponse;
import com.fitness.fitnessaicoach.dto.UserRequest;
import com.fitness.fitnessaicoach.dto.UserResponse;
import com.fitness.fitnessaicoach.service.AuthService;
import com.fitness.fitnessaicoach.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", security = {})
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.create(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and return JWT", security = {})
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
