# Multi-stage Dockerfile for !Adventure
# Stage 1: Build stage
FROM eclipse-temurin:21-jdk AS builder

# Set working directory
WORKDIR /build

# Copy Maven wrapper and pom.xml first (for caching)
COPY maven/ ./maven/
COPY pom.xml .

# Make Maven wrapper scripts executable (Linux requires this)
RUN chmod +x ./maven/mvn/bin/mvn ./maven/mvn/bin/mvnDebug

# Download dependencies (cached layer if pom.xml unchanged)
RUN ./maven/mvn/bin/mvn dependency:go-offline -B

# Copy source code
COPY src/ ./src/

# Build the application (skip tests in Docker build for speed)
RUN ./maven/mvn/bin/mvn package -DskipTests=true

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre

# Set working directory
WORKDIR /app

# Copy the executable JAR from builder stage
COPY --from=builder /build/target/adventure-0.1.0-SNAPSHOT.jar /app/adventure.jar

# Create directories for saves and configuration
RUN mkdir -p /app/saves /app/config /app/logs

# Expose default server port
EXPOSE 8080

# Set default environment variables
ENV ADVENTURE_PORT=8080 \
    ADVENTURE_MODE=server \
    JAVA_OPTS="-Xmx2g -Xms512m"

# Health check (verify server is responding)
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD timeout 5 bash -c '</dev/tcp/localhost/8080' || exit 1

# Default command: run server
# Can be overridden with docker run arguments
CMD java $JAVA_OPTS -jar /app/adventure.jar --server --port ${ADVENTURE_PORT}

# Alternative usage examples:
# Run world viewer: docker run adventure:latest java -jar /app/adventure.jar --width 60 --height 25 --seed 12345
# Run interactive client: docker run -it adventure:latest java -jar /app/adventure.jar --interactive
