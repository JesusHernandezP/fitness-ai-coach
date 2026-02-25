package com.fitness.fitnessaicoach.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue
    private UUID id;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    private String passwordHash;

    private Integer age;

    private Integer heightCm;

    private Integer weightKg;

    private LocalDateTime createdAt = LocalDateTime.now();

    public User() {}

    public User(String name, String email) {
        this.name = name;
        this.email = email;
        this.createdAt = LocalDateTime.now();
    }

    // Getters y setters

    public UUID getId() { return id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }

    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Integer getAge() { return age; }

    public void setAge(Integer age) { this.age = age; }

    public Integer getHeightCm() { return heightCm; }

    public void setHeightCm(Integer heightCm) { this.heightCm = heightCm; }

    public Integer getWeightKg() { return weightKg; }

    public void setWeightKg(Integer weightKg) { this.weightKg = weightKg; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}