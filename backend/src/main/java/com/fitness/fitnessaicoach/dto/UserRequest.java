package com.fitness.fitnessaicoach.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {

    @NotBlank(message = "El nombre es obligatorio.")
    private String name;

    @NotBlank(message = "El email es obligatorio.")
    @Email(message = "El email no tiene un formato válido.")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria.")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres.")
    private String password;

    @Positive(message = "La edad debe ser un número positivo.")
    @Max(value = 120, message = "La edad no puede superar los 120 años.")
    private Integer age;

    @Positive(message = "La altura debe ser positiva.")
    private Double heightCm;

    @Positive(message = "El peso debe ser positivo.")
    private Double weightKg;
}