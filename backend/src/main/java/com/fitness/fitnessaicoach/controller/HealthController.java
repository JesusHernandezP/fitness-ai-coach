package com.fitness.fitnessaicoach.controller;

import com.fitness.fitnessaicoach.ai.provider.groq.GroqClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HealthController {

    private final GroqClient groqClient;

    @GetMapping("/api/health")
    public String health() {
        return "OK";
    }

    @GetMapping("/api/health/groq")
    public GroqClient.ConnectionStatus groqHealth() {
        return groqClient.checkConnectivity();
    }
}
