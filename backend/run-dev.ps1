$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
Set-Location "$root\backend"

<<<<<<< HEAD
$composeFile = Join-Path $PWD "docker-compose.yml"
$dbHost = if ($env:DB_HOST) { $env:DB_HOST } else { "127.0.0.1" }
$dbPort = if ($env:DB_PORT) { [int]$env:DB_PORT } else { 5432 }
$dbName = if ($env:DB_NAME) { $env:DB_NAME } else { "fitness_db" }
$dbWaitSeconds = 45
$postgresContainerName = "fitness-postgres"

if (Get-Command docker -ErrorAction SilentlyContinue) {
    if (Test-Path $composeFile) {
        Write-Host "Levantando PostgreSQL con Docker Compose..."
        docker compose up -d postgres | Out-Host
    }
} else {
    Write-Warning "Docker no esta disponible en PATH. El backend requiere PostgreSQL en $dbHost`:$dbPort."
}

$dbReady = $false
for ($i = 0; $i -lt $dbWaitSeconds; $i++) {
    try {
        $tcpClient = [System.Net.Sockets.TcpClient]::new()
        $asyncResult = $tcpClient.BeginConnect($dbHost, $dbPort, $null, $null)
        if ($asyncResult.AsyncWaitHandle.WaitOne(1000) -and $tcpClient.Connected) {
            $tcpClient.EndConnect($asyncResult)
            $tcpClient.Close()
            $dbReady = $true
            break
        }
        $tcpClient.Close()
    } catch {
    }
    Start-Sleep -Seconds 1
}

if (-not $dbReady) {
    throw "PostgreSQL no esta disponible en $dbHost`:$dbPort tras esperar $dbWaitSeconds segundos."
}

if (-not (Test-Path "target\fitness-ai-coach-0.0.1-SNAPSHOT.jar")) {
    Write-Host "No se encontro el jar empaquetado. Ejecutando paquete de Maven..."
    .\mvnw.cmd -DskipTests package
=======
if (-not (Test-Path "target\fitness-ai-coach-0.0.1-SNAPSHOT.jar")) {
    Write-Host "No se encontro el jar empaquetado. Ejecutando paquete de Maven..."
    mvn -DskipTests package
>>>>>>> main
}

if (-not $env:GROQ_API_KEY) {
    Write-Host "Aviso: GROQ_API_KEY no esta configurada. Puedes exportarla con:"
    Write-Host '$env:GROQ_API_KEY="TU_API_KEY"'
}

<<<<<<< HEAD
if (-not $env:DB_URL) {
    $env:DB_URL = "jdbc:postgresql://${dbHost}:$dbPort/${dbName}?sslmode=disable"
}

if (-not $env:DB_USERNAME) {
    $env:DB_USERNAME = "postgres"
}

if (-not $env:DB_PASSWORD) {
    $env:DB_PASSWORD = "1234"
=======
if (-not $env:JWT_SECRET) {
    $env:JWT_SECRET = [Convert]::ToBase64String((1..48 | ForEach-Object { Get-Random -Minimum 0 -Maximum 256 }))
    Write-Host "Aviso: JWT_SECRET no estaba configurada. Se genero un valor temporal solo para esta sesion."
>>>>>>> main
}

if (-not $env:GROQ_MODEL) {
    $env:GROQ_MODEL = "llama-3.1-8b-instant"
}

<<<<<<< HEAD
=======
if (-not $env:SPRING_DATASOURCE_URL) {
    $env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:5432/fitness_db"
}

if (-not $env:SPRING_DATASOURCE_USERNAME) {
    $env:SPRING_DATASOURCE_USERNAME = "postgres"
}

if (-not $env:SPRING_DATASOURCE_PASSWORD) {
    $env:SPRING_DATASOURCE_PASSWORD = "1234"
}

if (-not $env:JWT_SECRET) {
    $env:JWT_SECRET = "dev-only-jwt-secret-key-with-32-bytes"
}

>>>>>>> main
Write-Host "Arrancando en modo DEV (Swagger habilitado)."
java '-Dspring.profiles.active=dev' -jar target\fitness-ai-coach-0.0.1-SNAPSHOT.jar
