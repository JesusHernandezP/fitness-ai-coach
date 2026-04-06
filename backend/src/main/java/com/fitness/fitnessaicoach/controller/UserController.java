package com.fitness.fitnessaicoach.controller;

import com.fitness.fitnessaicoach.dto.MetabolicProfileResponse;
import com.fitness.fitnessaicoach.service.MetabolicProfileService;
import com.fitness.fitnessaicoach.dto.UserProfileUpdateRequest;
import com.fitness.fitnessaicoach.dto.UserRequest;
import com.fitness.fitnessaicoach.dto.UserResponse;
import com.fitness.fitnessaicoach.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Users", description = "User management endpoints")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final MetabolicProfileService metabolicProfileService;

    @PostMapping
    @Operation(summary = "Register a new user", security = {})
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        UserResponse created = userService.create(request);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by id")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getUserById(
            @PathVariable String id,
            @AuthenticationPrincipal String email
    ) {
        if ("profile".equals(id)) {
            MetabolicProfileResponse profile = metabolicProfileService.getProfile(email);
            return ResponseEntity.ok(profile);
        }

        UserResponse user = userService.getById(UUID.fromString(id));
        return ResponseEntity.ok(user);
    }

    @GetMapping
    @Operation(summary = "Get all users")
    @SecurityRequirement(name = "bearerAuth")
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update current user metabolic profile")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> updateUserProfile(
            @PathVariable String id,
            @Valid @RequestBody UserProfileUpdateRequest request,
            @AuthenticationPrincipal String email
    ) {
        if ("profile".equals(id)) {
            MetabolicProfileResponse profile = metabolicProfileService.updateProfile(
                    email,
                    com.fitness.fitnessaicoach.dto.MetabolicProfileRequest.builder()
                            .age(request.getAge())
                            .heightCm(request.getHeightCm())
                            .sex(request.getSex())
                            .activityLevel(request.getActivityLevel())
                            .build()
            );
            return ResponseEntity.ok(profile);
        }

        UserResponse updated = userService.updateProfile(UUID.fromString(id), email, request);
        return ResponseEntity.ok(updated);
    }
}
