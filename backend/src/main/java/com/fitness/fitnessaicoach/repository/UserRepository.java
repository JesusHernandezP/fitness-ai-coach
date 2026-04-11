package com.fitness.fitnessaicoach.repository;

import com.fitness.fitnessaicoach.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByIdAndEmail(UUID id, String email);

    boolean existsByEmail(String email);
}
