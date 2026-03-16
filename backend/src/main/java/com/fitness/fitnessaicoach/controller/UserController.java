package com.fitness.fitnessaicoach.controller;

import com.fitness.fitnessaicoach.dto.UserRequest;
import com.fitness.fitnessaicoach.dto.UserResponse;
import com.fitness.fitnessaicoach.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Users", description = "User management endpoints")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management endpoints")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Create a new user")
    @PostMapping
    @Operation(summary = "Register a new user", security = {})
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        UserResponse created = userService.create(request);
        return ResponseEntity.ok(created);
    }
    @Operation(summary = "Get user by id")
    @GetMapping("/{id}")
    @Operation(summary = "Get user by id")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        UserResponse user = userService.getById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    @Operation(summary = "Get all users")
    @SecurityRequirement(name = "bearerAuth")
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }
}
