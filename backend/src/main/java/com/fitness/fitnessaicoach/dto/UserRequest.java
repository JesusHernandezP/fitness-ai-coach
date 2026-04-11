package com.fitness.fitnessaicoach.dto;

import com.fitness.fitnessaicoach.domain.ActivityLevel;
import com.fitness.fitnessaicoach.domain.UserSex;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {

    @NotBlank(message = "El nombre es obligatorio.")
    private String name;

    @NotBlank(message = "El email es obligatorio.")
    @Email(message = "El email no tiene un formato valido.")
    private String email;

    @NotBlank(message = "La contrasena es obligatoria.")
    @Size(min = 6, message = "La contrasena debe tener al menos 6 caracteres.")
    private String password;

    @Min(value = 15, message = "Age must be at least 15.")
    @Max(value = 80, message = "Age cannot be greater than 80.")
    private Integer age;

    @DecimalMin(value = "120.0", message = "Height must be at least 120 cm.")
    @DecimalMax(value = "220.0", message = "Height cannot be greater than 220 cm.")
    private Double heightCm;

    @Positive(message = "El peso debe ser positivo.")
    private Double weightKg;

    private UserSex sex;
    private ActivityLevel activityLevel;
}
