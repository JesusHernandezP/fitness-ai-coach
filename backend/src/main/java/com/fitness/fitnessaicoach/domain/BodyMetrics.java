package com.fitness.fitnessaicoach.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
<<<<<<< HEAD
@Table(
        name = "body_metrics",
        uniqueConstraints = @UniqueConstraint(
                name = "unique_body_metrics_user_date",
                columnNames = {"user_id", "date"}
        )
)
=======
@Table(name = "body_metrics")
>>>>>>> main
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BodyMetrics {

    @Id
<<<<<<< HEAD
    @GeneratedValue(strategy = GenerationType.UUID)
=======
    @GeneratedValue
>>>>>>> main
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Double weight;

<<<<<<< HEAD
=======
    private Double bodyFat;

    private Double muscleMass;

>>>>>>> main
    @Column(nullable = false)
    private LocalDate date;
}
