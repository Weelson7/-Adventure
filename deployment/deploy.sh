#!/bin/bash
# !Adventure Deployment Script for Linux/macOS
# Version: 0.1.0
# Purpose: Build, test, package, and optionally deploy the game

set -e

# Change to repository root (parent of deployment folder)
cd "$(dirname "$0")/.."

# Parse arguments
SKIP_TESTS=false
SKIP_COVERAGE=false
BUILD_DOCKER=false
RUN_SERVER=false
CLEAN=true
PORT=8080
DOCKER_TAG="adventure:latest"

while [[ $# -gt 0 ]]; do
    case $1 in
        --skip-tests)
            SKIP_TESTS=true
            shift
            ;;
        --skip-coverage)
            SKIP_COVERAGE=true
            shift
            ;;
        --build-docker)
            BUILD_DOCKER=true
            shift
            ;;
        --run-server)
            RUN_SERVER=true
            shift
            ;;
        --no-clean)
            CLEAN=false
            shift
            ;;
        --port)
            PORT="$2"
            shift 2
            ;;
        --docker-tag)
            DOCKER_TAG="$2"
            shift 2
            ;;
        *)
            echo "Unknown option: $1"
            echo "Usage: $0 [--skip-tests] [--skip-coverage] [--build-docker] [--run-server] [--no-clean] [--port PORT] [--docker-tag TAG]"
            exit 1
            ;;
    esac
done

echo "========================================"
echo "!Adventure Deployment Script (Linux)"
echo "========================================"
echo ""

# Detect Maven wrapper
if [ -f "./maven/mvn/bin/mvn" ]; then
    MVN_CMD="./maven/mvn/bin/mvn"
elif command -v mvn &> /dev/null; then
    MVN_CMD="mvn"
else
    echo "ERROR: Maven not found!"
    exit 1
fi

echo "Using Maven: $MVN_CMD"

# Step 1: Clean (optional)
if [ "$CLEAN" = true ]; then
    echo ""
    echo "[1/5] Cleaning previous build artifacts..."
    $MVN_CMD clean
    echo "✓ Clean complete"
else
    echo ""
    echo "[1/5] Skipping clean (use without --no-clean to enable)"
fi

# Step 2: Build and Test
echo ""
if [ "$SKIP_TESTS" = true ]; then
    echo "[2/5] Building (tests skipped)..."
    $MVN_CMD compile -DskipTests=true
else
    echo "[2/5] Building and running tests..."
    $MVN_CMD test
fi
echo "✓ Build and tests complete"

# Step 3: Coverage Report (optional)
if [ "$SKIP_COVERAGE" = false ] && [ "$SKIP_TESTS" = false ]; then
    echo ""
    echo "[3/5] Generating coverage report..."
    $MVN_CMD jacoco:report || echo "WARNING: Coverage report generation failed"
    echo "✓ Coverage report generated: target/site/jacoco/index.html"
else
    echo ""
    echo "[3/5] Skipping coverage report"
fi

# Step 4: Package executable JAR
echo ""
echo "[4/5] Packaging executable JAR..."
$MVN_CMD package -DskipTests=true
echo "✓ Executable JAR created: target/adventure-0.1.0-SNAPSHOT.jar"

# Step 5: Docker Build (optional)
if [ "$BUILD_DOCKER" = true ]; then
    echo ""
    echo "[5/5] Building Docker image..."
    docker build -f deployment/Dockerfile -t "$DOCKER_TAG" .
    echo "✓ Docker image built: $DOCKER_TAG"
else
    echo ""
    echo "[5/5] Skipping Docker build (use --build-docker to enable)"
fi

# Summary
echo ""
echo "========================================"
echo "Deployment Complete!"
echo "========================================"
echo ""
echo "Executable JAR: target/adventure-0.1.0-SNAPSHOT.jar"
echo ""
echo "Run the game:"
echo "  Server mode:  java -jar target/adventure-0.1.0-SNAPSHOT.jar --server --port $PORT"
echo "  Client mode:  java -jar target/adventure-0.1.0-SNAPSHOT.jar --interactive"
echo "  World viewer: java -jar target/adventure-0.1.0-SNAPSHOT.jar --width 60 --height 25 --seed 12345"
echo ""

# Auto-start server if requested
if [ "$RUN_SERVER" = true ]; then
    echo "Starting server on port $PORT..."
    echo ""
    java -jar target/adventure-0.1.0-SNAPSHOT.jar --server --port "$PORT"
fi
