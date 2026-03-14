# AI Backend Architecture

## Context
Fitness AI Coach is a backend service for nutrition and fitness tracking with future AI coaching integration.

Current implementation includes:
- User
- Exercise
- Food
- DailyLog
- Meal
- MealItem

The architecture stays modular and package-based to allow AI services to call business services without touching repositories.

## Layered Architecture
1. `controller`
   - Input/output contracts and HTTP behavior.
   - No repository access.
2. `service`
   - Business logic, validation orchestration, mapping, and composition.
3. `repository`
   - Database persistence abstraction via Spring Data JPA.
4. `domain`
   - JPA entities, UUID keys, relationships.
5. `dto`
   - Request/response models.
6. `exception`
   - Domain-specific exceptions and global handling.

## Data Contracts Rule
- All API endpoints return DTOs (`*Response`) and accept `*Request`.
- IDs are exposed as UUID strings.
- Relationships are represented by IDs in responses, not as nested JPA objects.

## Current Module Mapping
- User
  - `User`, `UserRepository`, `UserService`, `UserController`, `UserRequest`, `UserResponse`
- Exercise
  - `Exercise`, `ExerciseRepository`, `ExerciseService`, `ExerciseController`, `ExerciseRequest`, `ExerciseResponse`
- Food
  - `Food`, `FoodRepository`, `FoodService`, `FoodController`, `FoodRequest`, `FoodResponse`
- DailyLog
  - `DailyLog`, `DailyLogRepository`, `DailyLogService`, `DailyLogController`, `DailyLogRequest`, `DailyLogResponse`
- Meal
  - `Meal`, `MealRepository`, `MealService`, `MealController`, `MealRequest`, `MealResponse`
- MealItem
  - `MealItem`, `MealItemRepository`, `MealItemService`, `MealItemController`, `MealItemRequest`, `MealItemResponse`

## Endpoint Contract Pattern
- `POST /api/{resource}` -> create, return DTO, `201`.
- `GET /api/{resource}` -> list, return DTO list, `200`.
- `GET /api/{resource}/{id}` when available -> return DTO, `200`.
- `DELETE /api/{resource}/{id}` -> remove and return `204`.

## Error and Validation Model
- Use bean validation in request DTOs.
- Global exception mapping in `GlobalExceptionHandler`.
- Standard 404 handling for missing resources through module-specific exceptions.
- Validation errors respond with 400 and structured error payload.

## AI/Autonomy Integration Guidance
- AI modules (future) should consume service methods only.
- AI services should remain separate from repository layer.
- Keep business workflows in service classes to avoid duplicating logic in controllers or AI adapters.

