# AGENTS.md - Fitness AI Coach

## Project overview
Multiplatform fitness coaching app where AI is the main interface.

Stack:
- Backend: Java 17, Spring Boot, Spring Security JWT, JPA, PostgreSQL, Flyway
- Android: Kotlin, Jetpack Compose, MVVM, Retrofit, Hilt, DataStore
- Web: Angular
- AI provider: Groq

AI role:
- nutrition coach
- fitness coach
- contextual assistant

Backend calculates:
- calories
- macros
- metabolic profile

LLM interprets context and provides coaching advice.

---

## Architecture rules (MANDATORY)

Layered architecture:

controller -> service -> repository

Rules:
- controllers contain no business logic
- services contain business logic
- repositories only access DB
- always use DTOs
- never expose entities directly
- use constructor injection
- services must be stateless

Packages:

controller
service
repository
domain
dto
security
config
ai

---

## Coding principles

Prefer:
- small focused classes
- explicit naming
- immutability when possible
- clear validation using Bean Validation
- minimal changes per task

Avoid:
- magic numbers
- static mutable state
- hidden side effects
- large refactors unless requested

---

## Security rules

Never hardcode:
- JWT secrets
- API keys
- DB credentials

Use environment variables.

Production requirements:
- Swagger disabled
- restricted CORS
- validated DTO inputs
- no sensitive logs

Never log:
- tokens
- passwords
- full AI payloads

---

## AI architecture

Backend builds structured context.
LLM does NOT calculate macros.

Prompt must separate:
- nutrition context
- activity context
- progress context

AI output must be practical and actionable.

---

## Testing expectations

Focus tests on:
- services
- security
- nutrition calculations
- main user flow

Avoid trivial tests.

Use mocks for AI provider.

CI must pass before merge.

---

## CI/CD rules

GitHub Actions pipelines:
- backend-ci
- frontend-ci
- android-ci

Do not introduce breaking changes.

Code must compile in CI environment.

Avoid local-only configurations.

---

## Change policy

Make minimal safe changes.

Do not:
- change architecture
- introduce new frameworks
- modify API contracts without reason

Always ensure:
- project compiles
- tests pass
- CI remains green

If unsure, prefer conservative solutions.
