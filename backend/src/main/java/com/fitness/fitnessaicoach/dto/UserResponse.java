package com.fitness.fitnessaicoach.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private UUID id;
    private String name;
    private String email;
    private Integer age;
    private Double heightCm;
    private Double weightKg;
    private LocalDateTime createdAt;
}