# Codebase Map

Project: Fitness AI Coach

Purpose:

Fitness AI Coach is a backend platform designed to power a mobile application that acts as a personal nutritionist and fitness coach in your pocket.

The system allows users to:

- track workouts
- track nutrition
- monitor body metrics
- record daily health activity
- receive AI-based coaching recommendations

The backend exposes a REST API used by:

- Android mobile app
- Admin web dashboard
- AI assistant integration

---

# High Level Architecture

The backend follows a layered architecture.

controller → HTTP endpoints  
service → business logic  
repository → database access  
domain → entities  
dto → API request/response models

Data flow:

Client  
↓  
Controller  
↓  
Service  
↓  
Repository  
↓  
Database

Controllers never access repositories directly.

---

# Security Model

Authentication uses JWT tokens.

Flow:

Login  
↓  
AuthController  
↓  
AuthService  
↓  
JWT generation  
↓  
Client sends token in Authorization header  
↓  
JwtFilter validates token

All protected endpoints are under:

/api/**

---

# Project Structure

Main package:

com.fitness.fitnessaicoach

Modules:

controller
domain
repository
service
dto
security
exception
config

---

# Domain Entities

Current entities:

User  
Exercise  
Food

Planned entities:

DailyLog  
Meal  
MealItem  
WorkoutSession  
BodyMetrics  
Goals  
AIAnalysis

The system centers around a concept called **DailyLog**.

DailyLog represents a single day of user activity.

A DailyLog will aggregate:

- meals
- workouts
- steps
- calories

---

# API Design

All endpoints follow:

/api/{resource}

Examples:

/api/users  
/api/exercises  
/api/foods

Controllers are responsible only for handling HTTP.

All business logic belongs to services.

---

# DTO Rules

Entities must never be exposed directly through API responses.

Use DTOs:

Request DTO → incoming data  
Response DTO → outgoing data

Example:

ExerciseRequest  
ExerciseResponse

---

# Error Handling

Global error handling is implemented with:

GlobalExceptionHandler

Custom exceptions include:

UserNotFoundException  
EmailAlreadyUsedException  
InvalidCredentialsException

Each module can define its own exceptions.

Example:

FoodNotFoundException

---

# Database

Database: PostgreSQL

Persistence:

Spring Data JPA

Repositories extend:

JpaRepository<Entity, UUID>

Entities use UUID identifiers.

---

# API Documentation

Swagger OpenAPI is configured.

Swagger UI allows testing endpoints.

---

# Development Workflow

Development follows a ticket-based workflow.

Each ticket must:

1. Create necessary DTOs
2. Implement service logic
3. Implement controller endpoints
4. Respect layered architecture
5. Follow existing naming conventions

---

# Feature Pattern

Every new feature should follow this pattern:

domain
repository
service
controller
dto

Example:

Exercise

Exercise.java  
ExerciseRepository.java  
ExerciseService.java  
ExerciseController.java  
ExerciseRequest.java  
ExerciseResponse.java

---

# AI Integration (Future Module)

The system will integrate AI services that analyze user data.

AI services will provide:

- nutrition recommendations
- workout suggestions
- progress analysis
- calorie adjustments

AI integration will use a dedicated service layer:

AIService

AI services must never access repositories directly.

AI services only interact with application services.

---

# Core Product Vision

Fitness AI Coach is designed to become:

"A personal nutritionist and fitness coach in your pocket."

The goal is to allow users to:

- log meals easily
- track workouts
- monitor health progress
- receive AI-powered coaching
- talk to an AI assistant about fitness and nutrition

Example user questions:

"What should I eat today?"

"How many calories should I consume?"

"How can I improve my training?"

"Analyze my weekly progress."

The backend stores the user's historical data and enables AI-driven coaching.

---

# Coding Principles

Always follow existing architecture.

Never bypass service layer.

Prefer simple implementations.

Keep modules isolated and easy to extend.

Maintain consistency with existing modules.
