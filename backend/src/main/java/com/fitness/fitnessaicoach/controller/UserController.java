package com.fitness.fitnessaicoach.controller;

import com.fitness.fitnessaicoach.domain.User;
import com.fitness.fitnessaicoach.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")

public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.findAll();
    }

    @PostMapping("/demo")
    public User createDemoUser() {
        return userService.createDemoUser();
    }
}