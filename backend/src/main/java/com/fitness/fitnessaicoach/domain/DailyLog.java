package com.fitness.fitnessaicoach.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "daily_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyLog {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private LocalDate logDate;

    @Column(nullable = false)
    private Integer caloriesConsumed;

    @Column(nullable = false)
    private Integer caloriesBurned;

    @Column(nullable = false)
    private Integer steps;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
