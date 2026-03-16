# Backend Rules

Document: Fitness AI Coach Backend
Date: 2026-03-14

## Core Runtime Rules
- Use Java 17 and Spring Boot 3.x.
- Keep all code in `backend/src/main/java/com/fitness/fitnessaicoach`.
- Keep endpoints under `/api`.
- Keep strict layering: `controller -> service -> repository`.
- DTOs must be used for external contracts.
- Never return entities directly from controllers.
- Use UUID as primary keys for persistent entities.

## Layer Responsibilities
- `controller`
  - Only handles HTTP mapping and response codes.
  - Calls corresponding service methods only.
- `service`
  - Contains all business rules and mapping to DTOs.
  - Calls repository directly.
- `repository`
  - Extends `JpaRepository<Entity, UUID>`.
  - No business logic.
- `domain`
  - Pure JPA entities and relations.
- `dto`
  - `*Request` for input, `*Response` for output.
- `exception`
  - Module-specific exceptions plus global handling.

## Security Rules
- Protected endpoints use Bearer JWT.
- `JwtFilter` validates `Authorization: Bearer <token>`.
- Do not modify security configuration unless requested.

## API Rules
- API docs should expose only DTO-based contracts.
- Validate request payloads with Bean Validation annotations.
- Use:
  - `201` on create
  - `200` on successful read
  - `204` on successful delete
  - `404` for missing resources
  - `400` for validation errors

## Persistence Rules
- Use `@ManyToOne` for parent references (ex: meal -> daily log, meal item -> meal/food).
- Use join column names explicit (`meal_id`, `food_id`, `daily_log_id`).
- Keep cascade/relation strategy explicit and consistent with existing modules.

## Ticket 018: MealItem Specific Rules
- New meal-item data:
  - `MealItem` links one `Meal` and one `Food`.
  - `quantity` must be > 0.
  - `calculatedCalories` should be computed in service as:
    `food.calories * (quantity / 100.0)`.
- Exposed endpoints:
  - `POST /api/meal-items`
  - `GET /api/meal-items`
  - `DELETE /api/meal-items/{id}`
- Maintain behavior and error shape already used by module exceptions.

