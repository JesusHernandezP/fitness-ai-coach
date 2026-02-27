package com.fitness.fitnessaicoach.repository;

import com.fitness.fitnessaicoach.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    // Para login en el futuro
    Optional<User> findByEmail(String email);

    // Para validación profesional
    boolean existsByEmail(String email);
}