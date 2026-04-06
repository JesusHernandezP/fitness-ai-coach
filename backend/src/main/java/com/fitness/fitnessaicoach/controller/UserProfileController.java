package com.fitness.fitnessaicoach.controller;

import com.fitness.fitnessaicoach.dto.MetabolicProfileRequest;
import com.fitness.fitnessaicoach.dto.MetabolicProfileResponse;
import com.fitness.fitnessaicoach.service.MetabolicProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User Profile", description = "Metabolic profile endpoints")
@RestController
@RequestMapping("/api/users/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final MetabolicProfileService metabolicProfileService;

    @PostMapping
    @Operation(summary = "Create current user metabolic profile")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<MetabolicProfileResponse> createProfile(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody MetabolicProfileRequest request
    ) {
        MetabolicProfileResponse response = metabolicProfileService.saveProfile(email, request);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping
    @Operation(summary = "Get current user metabolic profile")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<MetabolicProfileResponse> getProfile(@AuthenticationPrincipal String email) {
        MetabolicProfileResponse response = metabolicProfileService.getProfile(email);
        return ResponseEntity.ok(response);
    }

    @PutMapping
    @Operation(summary = "Update current user metabolic profile")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<MetabolicProfileResponse> updateProfile(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody MetabolicProfileRequest request
    ) {
        MetabolicProfileResponse response = metabolicProfileService.updateProfile(email, request);
        return ResponseEntity.ok(response);
    }
}
