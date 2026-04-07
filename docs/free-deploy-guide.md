# Free Deploy Guide

This project can be deployed with a zero-cost setup for a demo or portfolio release.

## Recommended stack

- Database: Neon Postgres
- Backend: Render web service
- Web: Vercel
- Android distribution: GitHub Releases for APK delivery

## Architecture

1. Deploy PostgreSQL on Neon.
2. Point Spring Boot to the Neon connection string.
3. Deploy the backend on Render from the `backend` directory.
4. Deploy Angular on Vercel from the `web` directory.
5. Build the Android APK locally or in GitHub Actions and publish it as a release asset.

## Backend variables

Set these variables in the backend host:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `JWT_SECRET`
- `GROQ_API_KEY`
- `APP_CORS_ALLOWED_ORIGINS`

## Web variables

If the frontend is deployed separately, point it to the backend public URL.

- `API_BASE_URL`

## Android variables

Set the backend base URL for release builds before generating the APK.

## Release checklist

1. Create a clean production database.
2. Run backend with Flyway enabled.
3. Verify signup, login, profile save, chat logging, and weight progress.
4. Build Angular in production mode.
5. Build an Android release APK.
6. Publish the web URL and APK in the repository release notes.

## Low-cost hardening

- Use a strong `JWT_SECRET`.
- Restrict CORS to the deployed web domain.
- Do not commit secrets to the repository.
- Keep Swagger disabled in production unless needed.
- Rotate API keys if a public demo environment is shared.
