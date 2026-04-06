# Android Coding Rules

These rules ensure consistency with backend architecture.

---

## General rules

Follow clean architecture structure.

Do not mix UI code with API code.

Keep responsibilities separated.

Avoid large classes.

Prefer small focused classes.

---

## Naming conventions

DTO classes must end with:

Dto

Example:

UserDto
DailyLogDto
FoodDto

---

Domain models must NOT contain framework annotations.

Domain models represent business concepts.

---

## ViewModel rules

ViewModels must:

call use cases

expose UI state

not call Retrofit directly

not contain DTO logic

---

## Repository rules

Repository interfaces belong to domain layer.

Repository implementations belong to data layer.

Example:

domain/repository/AuthRepository

data/repository/AuthRepositoryImpl

---

## DTO mapping

DTO mapping must be explicit.

Use mapper classes.

Example:

UserDto → User

FoodDto → Food

---

## Coroutines

Use suspend functions for API calls.

Use Flow for observable data.

---

## Dependency Injection

Use Hilt.

Avoid manual instantiation.

---

## Retrofit rules

All API calls must be defined in:

data/remote/api

Use consistent endpoint naming matching backend.

---

## Error handling

Use Result wrapper class.

Example:

Result.Success
Result.Error

---

## Important

Maintain same naming as backend DTOs.

Example:

DailyLogResponse → DailyLogDto

FoodResponse → FoodDto