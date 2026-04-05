package com.fitness.fitnessaicoach.service;

import com.fitness.fitnessaicoach.domain.BodyMetrics;
import com.fitness.fitnessaicoach.domain.User;
import com.fitness.fitnessaicoach.dto.BodyMetricsRequest;
import com.fitness.fitnessaicoach.dto.BodyMetricsResponse;
import com.fitness.fitnessaicoach.exception.BodyMetricsAlreadyExistsException;
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

    public BodyMetricsResponse createBodyMetrics(String email, BodyMetricsRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found."));

        if (bodyMetricsRepository.existsByUserIdAndDate(user.getId(), request.getDate())) {
            throw new BodyMetricsAlreadyExistsException("Body metrics for this date already exist.");
        }

        BodyMetrics bodyMetrics = BodyMetrics.builder()
                .user(user)
                .weight(request.getWeight())
                .date(request.getDate())
                .build();

        BodyMetrics saved = bodyMetricsRepository.save(bodyMetrics);

        return toResponse(saved);
    }

    public List<BodyMetricsResponse> getAllBodyMetrics(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found."));

        return bodyMetricsRepository.findAllByUserIdOrderByDateDescIdDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public BodyMetricsResponse getBodyMetricsById(String email, UUID id) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found."));

        BodyMetrics bodyMetrics = bodyMetricsRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new BodyMetricsNotFoundException("Body metrics not found."));

        return toResponse(bodyMetrics);
    }

    public void deleteBodyMetrics(String email, UUID id) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found."));

        BodyMetrics bodyMetrics = bodyMetricsRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new BodyMetricsNotFoundException("Body metrics not found."));

        bodyMetricsRepository.delete(bodyMetrics);
    }

    private BodyMetricsResponse toResponse(BodyMetrics bodyMetrics) {
        return BodyMetricsResponse.builder()
                .id(bodyMetrics.getId())
                .userId(bodyMetrics.getUser() != null ? bodyMetrics.getUser().getId() : null)
                .weight(bodyMetrics.getWeight())
                .date(bodyMetrics.getDate())
                .build();
    }
}
