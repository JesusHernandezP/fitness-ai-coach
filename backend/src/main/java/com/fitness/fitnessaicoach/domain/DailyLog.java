package com.fitness.fitnessaicoach.domain;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "daily_logs",
        uniqueConstraints = @UniqueConstraint(name = "uk_daily_logs_user_date", columnNames = {"user_id", "log_date"})
)
@Getter
@Setter
public class DailyLog {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private LocalDate logDate;

    private Integer steps;
    private Double caloriesConsumed;
    private Double caloriesBurned;
    private Double weightKg;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
