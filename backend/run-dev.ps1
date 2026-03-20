$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
Set-Location "$root\backend"

if (-not (Test-Path "target\fitness-ai-coach-0.0.1-SNAPSHOT.jar")) {
    Write-Host "No se encontro el jar empaquetado. Ejecutando paquete de Maven..."
    mvn -DskipTests package
}

if (-not $env:GROQ_API_KEY) {
    Write-Host "Aviso: GROQ_API_KEY no esta configurada. Puedes exportarla con:"
    Write-Host '$env:GROQ_API_KEY="TU_API_KEY"'
}

if (-not $env:GROQ_MODEL) {
    $env:GROQ_MODEL = "llama-3.1-8b-instant"
}

Write-Host "Arrancando en modo DEV (Swagger habilitado)."
java '-Dspring.profiles.active=dev' -jar target\fitness-ai-coach-0.0.1-SNAPSHOT.jar
