# Loads variables from the root .env into this session, then starts the backend.
# Usage (from the repo root):   ./run-backend.ps1
# Spring Boot does not read .env natively, so we export the values first and let
# application.properties resolve its ${VAR} placeholders from the environment.

$ErrorActionPreference = "Stop"
$envFile = Join-Path $PSScriptRoot ".env"

if (-not (Test-Path $envFile)) {
    Write-Error "No .env found at repo root. Copy .env.example to .env and fill it in first."
}

Get-Content $envFile | ForEach-Object {
    $line = $_.Trim()
    if ($line -eq "" -or $line.StartsWith("#")) { return }
    $idx = $line.IndexOf("=")
    if ($idx -lt 1) { return }
    $name  = $line.Substring(0, $idx).Trim()
    $value = $line.Substring($idx + 1).Trim().Trim('"').Trim("'")
    Set-Item -Path "Env:$name" -Value $value
    Write-Host "loaded $name"
}

Write-Host "Starting backend on http://localhost:8080 ..."
Set-Location (Join-Path $PSScriptRoot "backend")
mvn spring-boot:run
