# Android Architecture — Fitness AI Coach

## Purpose

The Android application is the mobile client for the Fitness AI Coach platform.

It consumes the Spring Boot backend API.

The goal is to provide a clean, scalable and maintainable mobile architecture aligned with the backend layered architecture.

The Android app must be easy to extend with new features such as:

authentication
daily logs
meals
workouts
AI coaching
progress tracking

---

## Architecture Style

Clean Architecture (simplified)

presentation → domain → data

Similar philosophy to backend:

controller → service → repository → domain/dto

---

## Layer Responsibilities

### presentation layer

UI logic

Jetpack Compose screens

ViewModels

state management

navigation

No business logic inside composables.

No API calls directly in UI.

---

### domain layer

business models

use cases

repository interfaces

independent from Retrofit or Android framework.

---

### data layer

Retrofit API definitions

DTO models

mappers

repository implementations

local storage (DataStore)

---

## Data Flow

UI → ViewModel → UseCase → Repository → API → Backend

DTO → mapped → domain model → used in UI

---

## Key principles

DTOs must match backend responses.

Domain models must not depend on Retrofit.

Mapping must happen in data layer.

ViewModels must not depend on Retrofit directly.

Use Coroutines for async operations.

Use Flow for observable state.

---

## Tech stack

Language:
Kotlin

UI:
Jetpack Compose

HTTP:
Retrofit

Serialization:
Kotlinx Serialization

Async:
Coroutines + Flow

DI:
Hilt

Navigation:
Navigation Compose

Local storage:
DataStore

---

## Project structure

android/fitness-ai-coach-app

presentation/
domain/
data/
di/
core/

---

## Long term vision

multi platform ready structure

possible future:

WearOS
Flutter client
web client alignment

---

## Important rule

Follow same naming conventions as backend.

Consistency improves maintainability.