# Android Development Guide for Codex

This project follows clean architecture.

Always respect the layer separation.

---

## Workflow

UI calls ViewModel.

ViewModel calls UseCase.

UseCase calls Repository interface.

Repository implementation calls Retrofit API.

DTO mapped to domain model.

Domain model returned to UI.

---

## Example flow

Login screen

LoginViewModel

LoginUseCase

AuthRepository

AuthRepositoryImpl

AuthApi

Backend endpoint:

POST /api/auth/login

---

## Retrofit configuration

Base URL:

http://10.0.2.2:8080/api/

10.0.2.2 allows Android emulator to access localhost backend.

---

## DTO mapping example

FoodResponse → FoodDto → Food

Mapping happens in:

data/remote/mapper

---

## Important

Do not place Retrofit logic in ViewModel.

Do not expose DTO directly to UI.

Use domain models in presentation layer.

---

## Folder creation rules

Always place classes in correct layer.

Do not mix domain and data.

---

## Coroutines

Use suspend functions for API calls.

Expose Flow for UI state.

---

## State management

Use simple UiState classes.

Example:

Loading
Success
Error

---

## Navigation

Use Navigation Compose.

Centralized navigation graph.

---

## Goal

Produce maintainable, readable and scalable code.