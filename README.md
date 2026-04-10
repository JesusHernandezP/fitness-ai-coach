# Fitness AI Coach API

API backend para una plataforma de seguimiento de habitos fitness con coaching inteligente usando Groq.
Este repositorio es un monorepo: existen modulos de frontend/mobile, pero actualmente se trabaja con validacion por API.

--------------------------------------------------
Que hace esta aplicacion?
--------------------------------------------------

- Gestiona usuarios, registros diarios y entidades relacionadas del entrenamiento.
- Calcula y expone analisis diarios de actividad/calorias.
- Genera recomendaciones con IA para un `daily-log` usando Groq.
- Expone endpoints de salud para validaciones rapidas.
- Usa autenticacion con JWT.

Estructura del repositorio

- `backend/`: API Java + Spring Boot (`fitness-ai-coach`).
- `mobile/`: proyecto Android (Kotlin + MVVM).
- `web/`: proyecto web en Angular.
- `docs/`: documentacion del proyecto.

--------------------------------------------------
Requisitos
--------------------------------------------------

- Java 17
- Maven 3.9+ (o `mvnw` si lo tienes disponible)
- PostgreSQL 16+ en ejecucion
- PowerShell o Bash
- Clave de Groq valida (`GROQ_API_KEY`)

--------------------------------------------------
Variables de entorno
--------------------------------------------------

La configuracion se carga desde:

`backend/src/main/resources/application.yml`

Variables clave:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `JWT_SECRET` (obligatorio fuera de tests, minimo 32 bytes)
- `GROQ_API_KEY` (obligatorio para coaching IA)
- `GROQ_MODEL` (opcional, por defecto `llama-3.1-8b-instant`)

Notas para PostgreSQL remoto:

- Neon, Render, Railway y Supabase suelen requerir SSL en la URL JDBC.
- Usa `?sslmode=require` o `&sslmode=require` segun la URL que te entregue el proveedor.
- La aplicacion fija zona horaria JDBC en `UTC`.
- Flyway puede inicializar una base remota vacia con las migraciones del proyecto.

Ejemplo en PowerShell:

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://ep-example.eu-central-1.aws.neon.tech/fitness_db?sslmode=require"
$env:SPRING_DATASOURCE_USERNAME="fitness_user"
$env:SPRING_DATASOURCE_PASSWORD="CHANGE_ME"
$env:JWT_SECRET="change-this-in-env-to-a-32-byte-minimum-secret-key"
$env:GROQ_API_KEY="TU_API_KEY_DE_GROQ"
$env:GROQ_MODEL="llama-3.1-8b-instant"
```

Ejemplo local en desarrollo:

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/fitness_db"
$env:SPRING_DATASOURCE_USERNAME="postgres"
$env:SPRING_DATASOURCE_PASSWORD="1234"
```

--------------------------------------------------
Levantar backend en desarrollo (con Swagger)
--------------------------------------------------
Desde la raiz del proyecto:

```powershell
.\backend\run-dev.ps1
```

El script:
- arranca el perfil `dev`
- habilita Swagger
- usa `llama-3.1-8b-instant` como modelo por defecto
- empaqueta el jar si no existe

Tambien puedes levantarlo manualmente (mismo modo dev):

```powershell
Set-Location backend
mvn -DskipTests package
java '-Dspring.profiles.active=dev' -jar target\fitness-ai-coach-0.0.1-SNAPSHOT.jar
```

Chequeos rapidos:

- `GET http://localhost:8080/api/health` -> responde `OK`.
- `GET http://localhost:8080/api/health/groq` -> valida conectividad con Groq.

--------------------------------------------------
Ejecutar tests
--------------------------------------------------

```powershell
Set-Location backend
mvn test
```

--------------------------------------------------
Probar con Swagger
--------------------------------------------------

URL:

`http://localhost:8080/swagger-ui/index.html`

--------------------------------------------------
Probar con Postman (sin frontend)
--------------------------------------------------

1) Registrar usuario

`POST http://localhost:8080/api/users`

Body:

```json
{
  "name": "Usuario Prueba",
  "email": "test@fitness.local",
  "password": "123456",
  "age": 28,
  "heightCm": 175,
  "weightKg": 72
}
```

2) Iniciar sesion y obtener JWT

`POST http://localhost:8080/api/auth/login`

Body:

```json
{
  "email": "test@fitness.local",
  "password": "123456"
}
```

Usa el `token` en `Authorization: Bearer <token>` para endpoints protegidos.

3) Crear un registro diario

`POST http://localhost:8080/api/daily-logs`

Body:

```json
{
  "logDate": "2026-03-20",
  "steps": 8300,
  "caloriesConsumed": 2300,
  "caloriesBurned": 600,
  "userId": "<USER_ID_DEL_PASO_1>"
}
```

4) Solicitar coaching IA para un registro

`GET http://localhost:8080/api/ai-coach/daily-log/<DAILY_LOG_ID>`

El `<DAILY_LOG_ID>` es el `id` de la respuesta del paso anterior.

Respuesta esperada:

- `analysis`: metricas y resumen del `daily-log`.
- `advice`: texto de recomendacion generado por IA.

--------------------------------------------------
Notas de diagnostico
--------------------------------------------------

- Si el endpoint de coaching sigue devolviendo el mensaje fallback, revisa:
  - que `GROQ_API_KEY` este en el proceso de Java que corre la app.
  - que `GET /api/health/groq` devuelva `available=true`.
  - que el modelo configurado sea compatible y activo.
- `GROQ_MODEL` te permite cambiar modelo sin tocar codigo.
- Este README esta pensado para pruebas API-first mientras no haya frontend publicado.
