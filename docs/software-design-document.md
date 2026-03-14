# Fitness AI Coach
Software Design Document

Author: Jesús Hernández  
Project: DAM Intermodular Project  
Date: 2026

---

# 1. Introducción

Fitness AI Coach es una plataforma digital diseñada para ayudar a los usuarios a gestionar su salud física y nutricional mediante el seguimiento de actividad, alimentación y recomendaciones generadas por inteligencia artificial.

El sistema permite registrar información diaria del usuario para posteriormente analizar su progreso y generar recomendaciones personalizadas.

El sistema se compone de tres aplicaciones principales:

- Backend API
- Aplicación móvil Android
- Panel web de administración

La arquitectura está diseñada para ser modular, escalable y segura.

---

# 2. Objetivos del sistema

El sistema permite a los usuarios:

- Registrar información personal
- Registrar comidas
- Registrar entrenamientos
- Registrar pasos diarios
- Calcular balance calórico
- Registrar métricas físicas
- Visualizar progreso físico
- Obtener recomendaciones de entrenamiento y nutrición mediante IA

---

# 3. Alcance del proyecto

Este proyecto forma parte del Proyecto Intermodular del ciclo DAM.

El objetivo es demostrar competencias en:

- desarrollo backend
- arquitectura de software
- diseño de bases de datos
- seguridad
- integración con APIs externas
- CI/CD
- testing automatizado

---

# 4. Arquitectura del sistema

El sistema sigue una arquitectura cliente-servidor.

Arquitectura general:

Mobile App  
│  
│ REST API  
▼  
Backend (Spring Boot)  
│  
│ ORM  
▼  
PostgreSQL

Adicionalmente:

Backend  
│  
│ API  
▼  
OpenAI API

El backend actúa como capa central que gestiona:

- autenticación
- lógica de negocio
- persistencia de datos
- integración con servicios externos

---

# 5. Stack tecnológico

## Backend

- Java 17
- Spring Boot
- Spring Security
- Spring Data JPA
- JWT Authentication
- Maven

## Base de datos

- PostgreSQL

## Frontend

Mobile App

- Android Kotlin
- MVVM Architecture

Admin Dashboard

- Angular

## Infraestructura

- Docker
- Docker Compose
- GitHub Actions (CI/CD)

## Testing

- JUnit
- MockMvc
- Postman
- Swagger

---

# 6. Arquitectura del backend

El backend sigue una arquitectura por capas.

Controller  
gestiona requests HTTP

Service  
contiene lógica de negocio

Repository  
acceso a datos

Domain  
entidades persistidas

DTO  
objetos de transferencia

Exception  
manejo global de errores

Security  
configuración de seguridad JWT

---

# 7. Concepto central del sistema

El sistema se basa en un concepto central: **DailyLog**

Metáfora:

La aplicación funciona como un diario del cuerpo.

Cada día representa una página donde se registran:

- comidas
- entrenamientos
- pasos
- calorías
- progreso físico

DailyLog será la entidad central del sistema.

---

# 8. Modelo de datos

## User

Representa al usuario del sistema.

Campos:

- id UUID
- name
- email
- passwordHash
- age
- heightCm
- weightKg
- createdAt

Relaciones:

User  
└── DailyLog  
└── Goals  
└── BodyMetrics

---

## DailyLog

Registro diario del usuario.

Campos:

- id
- logDate
- caloriesConsumed
- caloriesBurned
- steps
- user

Relaciones futuras:

DailyLog  
└── Meal  
└── WorkoutSession  
└── AIAnalysis

---

## Food

Base de datos nutricional.

Campos:

- id
- name
- calories
- protein
- carbs
- fat

Ejemplos:

- Chicken breast
- Rice
- Egg
- Banana

---

## Meal

Representa una comida del usuario.

Tipos:

- Breakfast
- Lunch
- Dinner
- Snack

Relación:

DailyLog  
└── Meal

---

## MealItem

Relaciona alimentos con comidas.

Ejemplo:

Meal = Lunch

MealItem  
└── Chicken breast (200g)  
└── Rice (150g)

Campos:

- id
- meal
- food
- quantity
- calculatedCalories

---

## Exercise

Base de datos de ejercicios.

Campos:

- id
- name
- muscleGroup
- description

Ejemplos:

- Bench Press
- Squat
- Pull Up

---

## WorkoutSession

Registro de entrenamiento del usuario.

Campos:

- id
- dailyLog
- exercise
- sets
- reps
- duration
- caloriesBurned

---

## BodyMetrics

Seguimiento del progreso físico.

Campos:

- id
- weight
- bodyFat
- muscleMass
- date
- user

---

## Goals

Objetivos del usuario.

Ejemplos:

- lose weight
- build muscle
- maintenance

Campos:

- id
- goalType
- targetWeight
- targetCalories
- user

---

# 9. Mapa completo de entidades

User  
├── DailyLog  
│   ├── Meal  
│   │   └── MealItem  
│   │        └── Food  
│   ├── WorkoutSession  
│   │        └── Exercise  
│   └── AIAnalysis  
│  
├── BodyMetrics  
└── Goals


---

# 10. Seguridad

El sistema implementa autenticación basada en JWT.

Proceso de autenticación:

Login  
↓  
Validación de credenciales  
↓  
Generación de token JWT  
↓  
Cliente envía token en header Authorization  
↓  
JwtFilter valida token  
↓  
Acceso a recursos protegidos

Las contraseñas se almacenan usando hashing BCrypt.

---

# 11. Integración con IA

El sistema integrará la API de OpenAI para generar recomendaciones personalizadas.

Casos de uso:

- sugerencias de entrenamiento
- recomendaciones nutricionales
- análisis de progreso
- ajuste de calorías diarias

---

# 12. Arquitectura de integración con IA

La IA no debe acceder directamente a la base de datos.

Se introduce un AI Service Layer.

Controller  
↓  
Service  
↓  
AIService  
↓  
OpenAI API

Ventajas:

- desacoplamiento
- testing sencillo
- posibilidad de cambiar proveedor de IA
- control completo de prompts

---

# 13. CI/CD

El proyecto utiliza GitHub Actions para integración continua.

Pipeline:

1 Checkout repository  
2 Setup Java  
3 Cache Maven dependencies  
4 Start PostgreSQL container  
5 Run tests  
6 Build application

Esto garantiza que cada cambio se compile correctamente.

---

# 14. Testing Strategy

El sistema implementa tres niveles de pruebas.

Unit Tests  
testing de lógica de negocio

Integration Tests  
testing de endpoints mediante MockMvc

Manual Tests  
testing de endpoints mediante Postman

---

# 15. Roadmap técnico

Backend

- módulo Exercise
- módulo DailyLog
- módulo Food
- módulo Meal
- módulo WorkoutSession
- módulo AIAnalysis

Frontend

- aplicación Android
- panel web Angular

---

# 16. Decisiones de arquitectura

ADR-1

Uso de arquitectura por capas para separar responsabilidades.

ADR-2

Uso de JWT para autenticación stateless.

ADR-3

Uso de PostgreSQL como base de datos principal.

ADR-4

Uso de Docker para entornos reproducibles.

ADR-5

Separación del módulo de IA mediante un servicio independiente (AIService).

---

