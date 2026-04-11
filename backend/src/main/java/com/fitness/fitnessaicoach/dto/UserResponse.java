package com.fitness.fitnessaicoach.dto;

<<<<<<< HEAD
import com.fitness.fitnessaicoach.domain.ActivityLevel;
import com.fitness.fitnessaicoach.domain.UserSex;
=======
>>>>>>> main
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private UUID id;
    private String name;
    private String email;
    private Integer age;
    private Double heightCm;
    private Double weightKg;
<<<<<<< HEAD
    private UserSex sex;
    private ActivityLevel activityLevel;
    private LocalDateTime createdAt;
}
=======
    private LocalDateTime createdAt;
}
>>>>>>> main
