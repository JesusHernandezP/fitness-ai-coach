package com.fitness.fitnessaicoach.domain;

import jakarta.persistence.*;
import lombok.*;
<<<<<<< HEAD

=======
>>>>>>> main
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

<<<<<<< HEAD
    @Column(name = "password_hash", nullable = false)
    private String password;
=======
    // Se guardará más adelante con BCrypt
    private String passwordHash;
>>>>>>> main

    private Integer age;

    private Double heightCm;
    private Double weightKg;

<<<<<<< HEAD
    @Enumerated(EnumType.STRING)
    private UserSex sex;

    @Enumerated(EnumType.STRING)
    private ActivityLevel activityLevel;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
=======
    @Column(nullable = false)
    private LocalDateTime createdAt;
}
>>>>>>> main
