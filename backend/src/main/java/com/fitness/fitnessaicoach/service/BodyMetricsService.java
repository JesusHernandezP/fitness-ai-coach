package com.fitness.fitnessaicoach.service;

import com.fitness.fitnessaicoach.domain.BodyMetrics;
import com.fitness.fitnessaicoach.domain.User;
import com.fitness.fitnessaicoach.dto.BodyMetricsRequest;
import com.fitness.fitnessaicoach.dto.BodyMetricsResponse;
import com.fitness.fitnessaicoach.exception.BodyMetricsNotFoundException;
import com.fitness.fitnessaicoach.exception.UserNotFoundException;
import com.fitness.fitnessaicoach.repository.BodyMetricsRepository;
import com.fitness.fitnessaicoach.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BodyMetricsService {

    private final BodyMetricsRepository bodyMetricsRepository;
    private final UserRepository userRepository;

    public BodyMetricsResponse createBodyMetrics(BodyMetricsRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found."));

        BodyMetrics bodyMetrics = BodyMetrics.builder()
                .user(user)
                .weight(request.getWeight())
                .bodyFat(request.getBodyFat())
                .muscleMass(request.getMuscleMass())
                .date(request.getDate())
                .build();

        BodyMetrics saved = bodyMetricsRepository.save(bodyMetrics);

        return toResponse(saved);
    }

    public List<BodyMetricsResponse> getAllBodyMetrics() {
        return bodyMetricsRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public BodyMetricsResponse getBodyMetricsById(UUID id) {
        BodyMetrics bodyMetrics = bodyMetricsRepository.findById(id)
                .orElseThrow(() -> new BodyMetricsNotFoundException("Body metrics not found."));

        return toResponse(bodyMetrics);
    }

    public void deleteBodyMetrics(UUID id) {
        BodyMetrics bodyMetrics = bodyMetricsRepository.findById(id)
                .orElseThrow(() -> new BodyMetricsNotFoundException("Body metrics not found."));

        bodyMetricsRepository.delete(bodyMetrics);
    }

    private BodyMetricsResponse toResponse(BodyMetrics bodyMetrics) {
        return BodyMetricsResponse.builder()
                .id(bodyMetrics.getId())
                .userId(bodyMetrics.getUser() != null ? bodyMetrics.getUser().getId() : null)
                .weight(bodyMetrics.getWeight())
                .bodyFat(bodyMetrics.getBodyFat())
                .muscleMass(bodyMetrics.getMuscleMass())
                .date(bodyMetrics.getDate())
                .build();
    }
}
