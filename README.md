# Fitness AI Coach

Aplicación multiplataforma que combina seguimiento fitness con inteligencia artificial para analizar el progreso del usuario y ofrecer recomendaciones personalizadas.

## Descripción

Fitness AI Coach es una aplicación que actúa como un nutricionista y entrenador personal digital.

Permite registrar datos de salud y entrenamiento y utiliza inteligencia artificial para interpretar el progreso y ofrecer recomendaciones útiles.

El objetivo es transformar datos aislados en información comprensible y accionable.

## Problema que resuelve

Muchas personas registran:

- comidas
- entrenamientos
- peso
- objetivos

pero estos datos quedan dispersos y no se transforman en conocimiento útil.

La aplicación centraliza la información y utiliza IA para generar análisis personalizados.

## Funcionalidades principales

### Registro de log diario

- calorías consumidas
- calorías quemadas
- pasos

### Registro de métricas corporales

- peso
- grasa corporal
- masa muscular

### Dashboard web

- gráfico de progreso de peso
- resumen semanal generado por IA
- chat con IA

### Chat persistente

- historial de conversación almacenado en base de datos

### Seguridad y datos

- autenticación segura con JWT
- base de datos Dockerizada con PostgreSQL
- migraciones automáticas con Flyway

## Arquitectura

El backend sigue una arquitectura limpia basada en:

`controller -> service -> repository`

Se utilizan DTOs para desacoplar la API del modelo de datos y mantener contratos estables.

El sistema está dividido en tres partes:

### Backend

- Spring Boot
- Spring Security
- Spring Data JPA
- PostgreSQL
- Flyway

### Frontend web

- Angular

### Aplicación móvil

- Android Kotlin
- Jetpack Compose

### Inteligencia artificial

- integración mediante proveedor Groq
- arquitectura preparada para cambiar de proveedor sin rehacer la lógica principal

## Stack tecnológico

### Backend

- Java 17
- Spring Boot
- Spring Security
- Spring Data JPA

### Base de datos

- PostgreSQL
- Flyway
- Docker

### Frontend

- Angular

### Mobile

- Kotlin
- Jetpack Compose

### IA

- Groq API

### Herramientas

- Git
- GitHub
- Maven
- Swagger

## Estructura del proyecto

- `/backend` -> API REST Spring Boot
- `/android` -> aplicación móvil Kotlin
- `/web` -> dashboard Angular
- `/docs` -> documentación del proyecto

## Cómo ejecutar el proyecto en local

### 1. Iniciar base de datos

Desde la carpeta `backend`:

```bash
docker compose up -d
```

### 2. Ejecutar backend

```bash
cd backend
mvn spring-boot:run
```

Notas:

- El backend usa PostgreSQL y ejecuta migraciones Flyway automáticamente al arrancar.
- Para funcionalidades de IA reales, conviene definir `GROQ_API_KEY` como variable de entorno.
- `JWT_SECRET` debe configurarse explícitamente fuera de desarrollo.

### 3. Ejecutar frontend Angular

```bash
cd web
npm install
npm start
```

### 4. Ejecutar aplicación Android

1. Abrir la carpeta `android` en Android Studio.
2. Sincronizar Gradle.
3. Ejecutar un emulador o un dispositivo físico.
4. Lanzar la aplicación.

## Documentación API

Swagger está disponible en entorno de desarrollo.

URL habitual:

`http://localhost:8080/swagger-ui/index.html`

## Demo del proyecto

Script de presentación:

[`docs/demo-script.md`](D:/pruebaCodex/fitness-ai-coach/docs/demo-script.md)

Guía de despliegue gratuito:

[`docs/free-deploy-guide.md`](D:/pruebaCodex/fitness-ai-coach/docs/free-deploy-guide.md)

## Posibles mejoras futuras

- integración con wearables
- recordatorios inteligentes
- análisis de imágenes de comida
- planes de entrenamiento automáticos

## Autor

Jesus Hernandez

Estudiante de FP Desarrollo de Aplicaciones Multiplataforma (DAM)

Proyecto intermodular
