package com.fitness.fitnessaicoach.service;

import com.fitness.fitnessaicoach.domain.User;
import com.fitness.fitnessaicoach.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User createDemoUser() {
        User user = new User("Demo User", "demo@example.com");

        if (!userRepository.existsByEmail(user.getEmail())) {
            return userRepository.save(user);
        }
        return userRepository.findAll().stream().findFirst().orElse(null);
    }
}