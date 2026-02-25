# Modelo de dominio – Fitness AI Coach

> Versión 0.1 – boceto inicial

## Entidades principales

- **User**
    - id (UUID)
    - name
    - email
    - passwordHash
    - role (USER / COACH / ADMIN)
    - age
    - heightCm
    - weightKg
    - createdAt

- **WorkoutPlan**
    - id (UUID)
    - name
    - description
    - level (BEGINNER / INTERMEDIATE / ADVANCED)
    - goal (LOSE_WEIGHT / GAIN_MUSCLE / MAINTAIN)
    - ownerCoachId (User.id)
    - createdAt

- **WorkoutSession**
    - id (UUID)
    - planId (WorkoutPlan.id)
    - dayNumber (1..7)
    - notes

- **Exercise**
    - id (UUID)
    - name
    - description
    - muscleGroup
    - equipmentRequired (boolean)

- **WorkoutExercise**
    - id (UUID)
    - workoutSessionId (WorkoutSession.id)
    - exerciseId (Exercise.id)
    - sets
    - reps
    - restSeconds

- **NutritionPlan** (para más adelante)
    - id (UUID)
    - userId (User.id)
    - caloriesPerDay
    - proteinGrams
    - carbsGrams
    - fatGrams

## Relaciones (simplificadas)

- Un **User** puede tener varios **WorkoutPlan** (si es coach).
- Un **WorkoutPlan** tiene varias **WorkoutSession** (por ejemplo, 4 días / semana).
- Cada **WorkoutSession** tiene varias **WorkoutExercise**.
- Cada **WorkoutExercise** referencia un **Exercise**.
- Un **User** puede tener un **NutritionPlan** activo.

## Alcance inicial (MVP backend)

Para la primera versión nos centraremos en:

1. **User**
2. **WorkoutPlan**
3. **WorkoutSession**
4. **Exercise**
5. **WorkoutExercise**

Auth con JWT y NutritionPlan se añaden en fases posteriores.