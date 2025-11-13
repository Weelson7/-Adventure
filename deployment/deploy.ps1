# !Adventure Deployment Script for Windows (PowerShell)
# Version: 0.1.0
# Purpose: Build, test, package, and optionally deploy the game

param(
    [switch]$SkipTests = $false,
    [switch]$SkipCoverage = $false,
    [switch]$BuildDocker = $false,
    [switch]$RunServer = $false,
    [switch]$Clean = $true,
    [string]$Port = "8080",
    [string]$DockerTag = "adventure:latest"
)

$ErrorActionPreference = "Stop"

# Change to repository root (parent of deployment folder)
Set-Location $PSScriptRoot\..

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "!Adventure Deployment Script (Windows)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Detect Maven wrapper
$mvnCmd = ".\maven\mvn\bin\mvn.cmd"
if (-not (Test-Path $mvnCmd)) {
    Write-Host "ERROR: Maven wrapper not found at $mvnCmd" -ForegroundColor Red
    exit 1
}

Write-Host "Using Maven: $mvnCmd" -ForegroundColor Green

# Step 1: Clean (optional)
if ($Clean) {
    Write-Host ""
    Write-Host "[1/5] Cleaning previous build artifacts..." -ForegroundColor Yellow
    & $mvnCmd clean
    if ($LASTEXITCODE -ne 0) {
        Write-Host "ERROR: Clean failed!" -ForegroundColor Red
        exit 1
    }
    Write-Host "✓ Clean complete" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "[1/5] Skipping clean (use -Clean to enable)" -ForegroundColor Gray
}

# Step 2: Build and Test
Write-Host ""
if ($SkipTests) {
    Write-Host "[2/5] Building (tests skipped)..." -ForegroundColor Yellow
    & $mvnCmd compile -DskipTests=true
} else {
    Write-Host "[2/5] Building and running tests..." -ForegroundColor Yellow
    & $mvnCmd test
}

if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Build/test failed!" -ForegroundColor Red
    exit 1
}
Write-Host "✓ Build and tests complete" -ForegroundColor Green

# Step 3: Coverage Report (optional)
if (-not $SkipCoverage -and -not $SkipTests) {
    Write-Host ""
    Write-Host "[3/5] Generating coverage report..." -ForegroundColor Yellow
    & $mvnCmd jacoco:report
    if ($LASTEXITCODE -ne 0) {
        Write-Host "WARNING: Coverage report generation failed" -ForegroundColor Yellow
    } else {
        Write-Host "✓ Coverage report generated: target\site\jacoco\index.html" -ForegroundColor Green
    }
} else {
    Write-Host ""
    Write-Host "[3/5] Skipping coverage report" -ForegroundColor Gray
}

# Step 4: Package executable JAR
Write-Host ""
Write-Host "[4/5] Packaging executable JAR..." -ForegroundColor Yellow
& $mvnCmd package -DskipTests=true
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Package failed!" -ForegroundColor Red
    exit 1
}
Write-Host "✓ Executable JAR created: target\adventure-0.1.0-SNAPSHOT.jar" -ForegroundColor Green

# Step 5: Docker Build (optional)
if ($BuildDocker) {
    Write-Host ""
    Write-Host "[5/5] Building Docker image..." -ForegroundColor Yellow
    docker build -f deployment\Dockerfile -t $DockerTag .
    if ($LASTEXITCODE -ne 0) {
        Write-Host "ERROR: Docker build failed!" -ForegroundColor Red
        exit 1
    }
    Write-Host "✓ Docker image built: $DockerTag" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "[5/5] Skipping Docker build (use -BuildDocker to enable)" -ForegroundColor Gray
}

# Summary
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Deployment Complete!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Executable JAR: target\adventure-0.1.0-SNAPSHOT.jar" -ForegroundColor White
Write-Host ""
Write-Host "Run the game:" -ForegroundColor White
Write-Host "  Server mode:  java -jar target\adventure-0.1.0-SNAPSHOT.jar --server --port $Port" -ForegroundColor Gray
Write-Host "  Client mode:  java -jar target\adventure-0.1.0-SNAPSHOT.jar --interactive" -ForegroundColor Gray
Write-Host "  World viewer: java -jar target\adventure-0.1.0-SNAPSHOT.jar --width 60 --height 25 --seed 12345" -ForegroundColor Gray
Write-Host ""

# Auto-start server if requested
if ($RunServer) {
    Write-Host "Starting server on port $Port..." -ForegroundColor Yellow
    Write-Host ""
    java -jar target\adventure-0.1.0-SNAPSHOT.jar --server --port $Port
}
