package com.fitness.fitnessaicoach.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "daily_logs")
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

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
