# Repository Index

Project: Fitness AI Coach

This document provides a quick index of the repository so AI agents can understand the structure of the backend.

---

# Backend Location

backend/src/main/java/com/fitness/fitnessaicoach

---

# Core Architecture

The backend follows a layered architecture:

controller → HTTP endpoints  
service → business logic  
repository → database access  
domain → entities  
dto → request/response models

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

Controllers must never access repositories directly.

---

# Existing Modules

User module

domain/User.java  
repository/UserRepository.java  
service/UserService.java  
controller/UserController.java  
dto/UserRequest.java  
dto/UserResponse.java

---

Exercise module

domain/Exercise.java  
repository/ExerciseRepository.java  
service/ExerciseService.java  
controller/ExerciseController.java  
dto/ExerciseRequest.java  
dto/ExerciseResponse.java

---

Food module

domain/Food.java  
repository/FoodRepository.java  
service/FoodService.java  
controller/FoodController.java  
dto/FoodRequest.java  
dto/FoodResponse.java

---

DailyLog module

domain/DailyLog.java  
repository/DailyLogRepository.java  
service/DailyLogService.java  
controller/DailyLogController.java  
dto/DailyLogRequest.java  
dto/DailyLogResponse.java

---

# Global Components

Security

security/JwtFilter.java  
security/JwtService.java  
security/SecurityConfig.java

---

Exception Handling

exception/GlobalExceptionHandler.java

Common exceptions:

UserNotFoundException  
InvalidCredentialsException  
EmailAlreadyUsedException

Modules may define their own exceptions.

---

# API Structure

All endpoints follow:

/api/{resource}

Examples:

/api/users  
/api/exercises  
/api/foods  
/api/daily-logs

---

# Database

Database: PostgreSQL

Persistence: Spring Data JPA

Repositories extend:

JpaRepository<Entity, UUID>

Primary keys use UUID.

---

# API Documentation

Swagger OpenAPI is configured.

Swagger UI can be used to test endpoints.

---

# Development Workflow

Features are implemented through development tickets.

Each feature must follow this pattern:

domain
repository
service
controller
dto
exception

Use existing modules as reference when implementing new modules.