package com.fitness.fitnessaicoach.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ai_recommendations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIRecommendation {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "daily_log_id", nullable = false)
    private UUID dailyLogId;

    @Lob
    @Column(name = "analysis_snapshot", nullable = false, columnDefinition = "TEXT")
    private String analysisSnapshot;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String advice;

    @Column(nullable = false)
    private String model;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
