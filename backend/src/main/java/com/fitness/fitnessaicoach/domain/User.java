package com.fitness.fitnessaicoach.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    // Se guardará más adelante con BCrypt
    private String passwordHash;

    private Integer age;

    private Double heightCm;
    private Double weightKg;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}