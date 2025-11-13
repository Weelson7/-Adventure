# !Adventure — Deployment Guide

**Version:** 0.1.0-SNAPSHOT  
**Last Updated:** November 13, 2025  
**Phase:** MVP Phase 1.10 — CI/CD & Deployment

---

## Overview

This guide covers all deployment options for !Adventure, including local development, staging, production, Docker containers, and cloud platforms. The game is designed to be cross-platform deployable on **Windows** and **Linux/macOS**.

### Quick Links
- [Prerequisites](#prerequisites)
- [Local Deployment](#local-deployment)
- [Docker Deployment](#docker-deployment)
- [Cloud Deployment](#cloud-deployment)
- [Configuration](#configuration)
- [Troubleshooting](#troubleshooting)

---

## Prerequisites

### Required Software

#### All Platforms
- **Java Development Kit (JDK) 21 LTS**
  - Download: [Eclipse Temurin](https://adoptium.net/)
  - Verify: `java -version` should report `21.0.x`

#### Optional (for Docker deployment)
- **Docker** 20.10+ and **Docker Compose** 2.0+
  - Download: [Docker Desktop](https://www.docker.com/products/docker-desktop/)
  - Verify: `docker --version` and `docker-compose --version`

### Repository Setup

```bash
# Clone the repository
git clone https://github.com/your-org/adventure.git
cd adventure

# Verify Maven wrapper
# Windows
.\maven\mvn\bin\mvn.cmd -v

# Linux/macOS
./maven/mvn/bin/mvn -version
```

---

## Local Deployment

### Windows (PowerShell)

#### Quick Deployment Script
```powershell
# One-command deployment (build, test, package)
.\deploy.ps1

# Skip tests for faster builds
.\deploy.ps1 -SkipTests

# Build Docker image
.\deploy.ps1 -BuildDocker

# Start server immediately after build
.\deploy.ps1 -RunServer

# Full options
.\deploy.ps1 -SkipTests -SkipCoverage -BuildDocker -Port 9090
```

#### Manual Build Steps
```powershell
# 1. Clean and build
.\maven\mvn\bin\mvn.cmd clean package

# 2. Run tests with coverage
.\maven\mvn\bin\mvn.cmd verify

# 3. View coverage report
start target\site\jacoco\index.html
```

#### Run the Game
```powershell
# Server mode (multiplayer)
java -jar target\adventure-0.1.0-SNAPSHOT.jar --server --port 8080

# Interactive client mode
java -jar target\adventure-0.1.0-SNAPSHOT.jar --interactive

# World viewer (ASCII art)
java -jar target\adventure-0.1.0-SNAPSHOT.jar --width 60 --height 25 --seed 12345

# Custom JVM options (increase heap size)
java -Xmx4g -Xms1g -jar target\adventure-0.1.0-SNAPSHOT.jar --server
```

### Linux/macOS (Bash)

#### Quick Deployment Script
```bash
# Make script executable (first time only)
chmod +x deploy.sh

# One-command deployment (build, test, package)
./deploy.sh

# Skip tests for faster builds
./deploy.sh --skip-tests

# Build Docker image
./deploy.sh --build-docker

# Start server immediately after build
./deploy.sh --run-server

# Full options
./deploy.sh --skip-tests --skip-coverage --build-docker --port 9090
```

#### Manual Build Steps
```bash
# 1. Clean and build
./maven/mvn/bin/mvn clean package

# 2. Run tests with coverage
./maven/mvn/bin/mvn verify

# 3. View coverage report
open target/site/jacoco/index.html  # macOS
xdg-open target/site/jacoco/index.html  # Linux
```

#### Run the Game
```bash
# Server mode (multiplayer)
java -jar target/adventure-0.1.0-SNAPSHOT.jar --server --port 8080

# Interactive client mode
java -jar target/adventure-0.1.0-SNAPSHOT.jar --interactive

# World viewer (ASCII art)
java -jar target/adventure-0.1.0-SNAPSHOT.jar --width 60 --height 25 --seed 12345

# Custom JVM options (increase heap size)
java -Xmx4g -Xms1g -jar target/adventure-0.1.0-SNAPSHOT.jar --server
```

---

## Docker Deployment

### Build Docker Image

#### Using Deployment Script
```bash
# Windows
.\deploy.ps1 -BuildDocker

# Linux/macOS
./deploy.sh --build-docker
```

#### Using Docker Directly
```bash
# Build image
docker build -t adventure:latest .

# Verify build
docker images | grep adventure
```

### Run with Docker

#### Server Mode
```bash
# Run server on default port 8080
docker run -d \
  --name adventure-server \
  -p 8080:8080 \
  -v $(pwd)/saves:/app/saves \
  adventure:latest

# Run server on custom port
docker run -d \
  --name adventure-server \
  -p 9090:8080 \
  -e ADVENTURE_PORT=8080 \
  -e JAVA_OPTS="-Xmx4g -Xms1g" \
  -v $(pwd)/saves:/app/saves \
  adventure:latest

# View logs
docker logs -f adventure-server

# Stop server
docker stop adventure-server
docker rm adventure-server
```

#### Interactive Client Mode
```bash
# Run interactive client
docker run -it --rm adventure:latest \
  java -jar /app/adventure.jar --interactive
```

#### World Viewer Mode
```bash
# Generate and view ASCII world
docker run --rm adventure:latest \
  java -jar /app/adventure.jar --width 60 --height 25 --seed 12345
```

### Docker Compose Deployment

#### Quick Start
```bash
# Copy environment template
cp .env.example .env

# Edit .env (optional)
# ADVENTURE_PORT=8080
# JAVA_OPTS=-Xmx2g -Xms512m

# Start production server
docker-compose up -d adventure-server

# View logs
docker-compose logs -f adventure-server

# Stop server
docker-compose down
```

#### Production + Staging
```bash
# Start both production and staging servers
docker-compose --profile staging up -d

# Production: http://localhost:8080
# Staging: http://localhost:8081

# Stop all
docker-compose --profile staging down
```

#### Rebuild After Code Changes
```bash
# Rebuild and restart
docker-compose up -d --build

# Force rebuild without cache
docker-compose build --no-cache
docker-compose up -d
```

---

## Cloud Deployment

### AWS EC2

#### Setup EC2 Instance
```bash
# 1. Launch EC2 instance (Amazon Linux 2 or Ubuntu 22.04)
# 2. Instance type: t3.medium (2 vCPU, 4 GB RAM) or larger
# 3. Security Group: Allow inbound TCP 8080 (or custom port)

# SSH into instance
ssh -i your-key.pem ec2-user@your-instance-ip

# Install Java 21
sudo yum install -y java-21-amazon-corretto-devel  # Amazon Linux
# OR
sudo apt update && sudo apt install -y openjdk-21-jdk  # Ubuntu

# Install Docker (optional)
sudo yum install -y docker  # Amazon Linux
sudo systemctl start docker
sudo usermod -aG docker ec2-user
```

#### Deploy Application
```bash
# Upload JAR to EC2
scp -i your-key.pem target/adventure-0.1.0-SNAPSHOT.jar ec2-user@your-instance-ip:~/

# SSH into instance and run
ssh -i your-key.pem ec2-user@your-instance-ip
java -jar adventure-0.1.0-SNAPSHOT.jar --server --port 8080

# Or run as systemd service (see Systemd Service section)
```

### Azure VM

#### Setup Azure VM
```bash
# 1. Create VM (Ubuntu 22.04 LTS)
az vm create \
  --resource-group adventure-rg \
  --name adventure-vm \
  --image UbuntuLTS \
  --size Standard_B2s \
  --admin-username azureuser \
  --generate-ssh-keys

# 2. Open port 8080
az vm open-port --port 8080 --resource-group adventure-rg --name adventure-vm

# 3. SSH into VM
ssh azureuser@<vm-public-ip>

# 4. Install Java 21
sudo apt update && sudo apt install -y openjdk-21-jdk
```

#### Deploy Application
```bash
# Upload JAR to Azure VM
scp target/adventure-0.1.0-SNAPSHOT.jar azureuser@<vm-public-ip>:~/

# SSH into VM and run
ssh azureuser@<vm-public-ip>
java -jar adventure-0.1.0-SNAPSHOT.jar --server --port 8080
```

### Google Cloud Platform (GCP)

#### Setup Compute Engine VM
```bash
# 1. Create VM instance
gcloud compute instances create adventure-vm \
  --zone=us-central1-a \
  --machine-type=e2-medium \
  --image-family=ubuntu-2204-lts \
  --image-project=ubuntu-os-cloud

# 2. Create firewall rule
gcloud compute firewall-rules create adventure-server \
  --allow tcp:8080 \
  --source-ranges 0.0.0.0/0 \
  --target-tags adventure

# 3. Add tag to VM
gcloud compute instances add-tags adventure-vm --tags adventure --zone us-central1-a

# 4. SSH into VM
gcloud compute ssh adventure-vm --zone us-central1-a

# 5. Install Java 21
sudo apt update && sudo apt install -y openjdk-21-jdk
```

#### Deploy Application
```bash
# Upload JAR to GCP VM
gcloud compute scp target/adventure-0.1.0-SNAPSHOT.jar adventure-vm:~/ --zone us-central1-a

# SSH into VM and run
gcloud compute ssh adventure-vm --zone us-central1-a
java -jar adventure-0.1.0-SNAPSHOT.jar --server --port 8080
```

### Kubernetes Deployment

#### Create Kubernetes Manifests

**deployment.yaml**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: adventure-server
spec:
  replicas: 2
  selector:
    matchLabels:
      app: adventure
  template:
    metadata:
      labels:
        app: adventure
    spec:
      containers:
      - name: adventure
        image: adventure:latest
        ports:
        - containerPort: 8080
        env:
        - name: JAVA_OPTS
          value: "-Xmx2g -Xms512m"
        volumeMounts:
        - name: saves
          mountPath: /app/saves
        resources:
          requests:
            memory: "1Gi"
            cpu: "500m"
          limits:
            memory: "4Gi"
            cpu: "2000m"
      volumes:
      - name: saves
        persistentVolumeClaim:
          claimName: adventure-saves-pvc
```

**service.yaml**
```yaml
apiVersion: v1
kind: Service
metadata:
  name: adventure-service
spec:
  type: LoadBalancer
  selector:
    app: adventure
  ports:
  - protocol: TCP
    port: 8080
    targetPort: 8080
```

#### Deploy to Kubernetes
```bash
# Apply manifests
kubectl apply -f deployment.yaml
kubectl apply -f service.yaml

# Check status
kubectl get pods
kubectl get services

# View logs
kubectl logs -f deployment/adventure-server
```

---

## Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `ADVENTURE_PORT` | 8080 | Server port |
| `ADVENTURE_MODE` | server | Run mode (server, client, interactive) |
| `JAVA_OPTS` | `-Xmx2g -Xms512m` | JVM options |

### JVM Tuning

#### Memory Settings
```bash
# Small server (1-10 players)
JAVA_OPTS="-Xmx1g -Xms256m"

# Medium server (10-50 players)
JAVA_OPTS="-Xmx4g -Xms1g"

# Large server (50+ players)
JAVA_OPTS="-Xmx8g -Xms2g"
```

#### Garbage Collection
```bash
# G1GC (default, recommended)
JAVA_OPTS="-Xmx4g -Xms1g -XX:+UseG1GC"

# ZGC (low latency, Java 21+)
JAVA_OPTS="-Xmx4g -Xms1g -XX:+UseZGC"
```

#### Performance Monitoring
```bash
# Enable JMX monitoring
JAVA_OPTS="-Xmx4g -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9010 -Dcom.sun.management.jmxremote.authenticate=false"

# Enable flight recorder
JAVA_OPTS="-Xmx4g -XX:StartFlightRecording=duration=60s,filename=profile.jfr"
```

### Systemd Service (Linux)

Create `/etc/systemd/system/adventure.service`:

```ini
[Unit]
Description=Adventure Game Server
After=network.target

[Service]
Type=simple
User=adventure
WorkingDirectory=/opt/adventure
ExecStart=/usr/bin/java -Xmx4g -Xms1g -jar /opt/adventure/adventure-0.1.0-SNAPSHOT.jar --server --port 8080
Restart=on-failure
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
```

```bash
# Enable and start service
sudo systemctl daemon-reload
sudo systemctl enable adventure
sudo systemctl start adventure

# Check status
sudo systemctl status adventure

# View logs
sudo journalctl -u adventure -f
```

---

## Troubleshooting

### Build Failures

#### "java version mismatch"
**Cause:** JDK version doesn't match `pom.xml` requirements (Java 21).

**Fix:**
```bash
# Verify Java version
java -version  # Should report 21.0.x

# Windows: Set JAVA_HOME
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"

# Linux/macOS: Set JAVA_HOME
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
```

#### "Maven wrapper not found"
**Cause:** Maven wrapper scripts missing.

**Fix:**
```bash
# Windows
git checkout maven/mvn/bin/mvn.cmd

# Linux/macOS
git checkout maven/mvn/bin/mvn
chmod +x maven/mvn/bin/mvn
```

### Runtime Failures

#### "OutOfMemoryError: Java heap space"
**Cause:** Insufficient heap memory for large worlds or many players.

**Fix:**
```bash
# Increase heap size
java -Xmx8g -Xms2g -jar target/adventure-0.1.0-SNAPSHOT.jar --server
```

#### "Address already in use"
**Cause:** Port 8080 already in use by another process.

**Fix:**
```bash
# Windows: Find and kill process using port 8080
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/macOS: Find and kill process
lsof -i :8080
kill -9 <PID>

# Or use a different port
java -jar target/adventure-0.1.0-SNAPSHOT.jar --server --port 9090
```

#### "Save file corrupted"
**Cause:** Disk write failure, power loss, or schema mismatch.

**Fix:**
```bash
# Restore from backup (backups stored in saves/backups/)
# Windows
copy saves\backups\world_backup_1.json saves\world.json

# Linux/macOS
cp saves/backups/world_backup_1.json saves/world.json
```

### Docker Failures

#### "docker: Cannot connect to the Docker daemon"
**Cause:** Docker service not running.

**Fix:**
```bash
# Windows/macOS: Start Docker Desktop

# Linux: Start Docker service
sudo systemctl start docker
```

#### "Image build failed: no space left on device"
**Cause:** Insufficient disk space.

**Fix:**
```bash
# Clean up unused Docker resources
docker system prune -a

# Check disk usage
docker system df
```

### Performance Issues

#### "World generation too slow (>10 seconds for 512x512)"
**Cause:** Expensive algorithms or insufficient CPU.

**Fix:**
1. Profile with Java Flight Recorder:
   ```bash
   java -XX:StartFlightRecording=duration=60s,filename=profile.jfr -jar target/adventure-0.1.0-SNAPSHOT.jar --width 512 --height 512 --seed 42
   ```
2. Analyze `profile.jfr` with JDK Mission Control or VisualVM
3. Optimize hot paths (see `BUILD.md` → Performance Benchmarks)

#### "Server lag with 10+ active regions"
**Cause:** Too many active regions or inefficient tick processing.

**Fix:**
1. Reduce active region count (adjust `backgroundTickRateMultiplier`)
2. Increase server resources (CPU, memory)
3. Profile and optimize tick processing

---

## Security Best Practices

### Production Deployment

1. **Use TLS/SSL:** Encrypt client-server communication
   - Use a reverse proxy (nginx, Apache) with SSL certificates
   - Let's Encrypt for free certificates

2. **Firewall Configuration:** Restrict access to game port
   ```bash
   # Linux: ufw
   sudo ufw allow 8080/tcp
   sudo ufw enable
   
   # Linux: iptables
   sudo iptables -A INPUT -p tcp --dport 8080 -j ACCEPT
   ```

3. **User Permissions:** Run server as non-root user
   ```bash
   # Create dedicated user
   sudo useradd -r -s /bin/false adventure
   
   # Set file ownership
   sudo chown -R adventure:adventure /opt/adventure
   ```

4. **Rate Limiting:** Prevent abuse with reverse proxy rate limits
   ```nginx
   # nginx rate limiting
   limit_req_zone $binary_remote_addr zone=adventure:10m rate=10r/s;
   
   server {
       location / {
           limit_req zone=adventure burst=20;
           proxy_pass http://localhost:8080;
       }
   }
   ```

5. **Monitor Logs:** Track suspicious activity
   ```bash
   # Watch logs for failed auth attempts
   sudo journalctl -u adventure -f | grep "Authentication failed"
   ```

---

## Monitoring & Observability

### Health Checks

```bash
# Check server is running
curl http://localhost:8080/health || echo "Server down"

# Docker health check
docker inspect adventure-server | grep -A 10 Health
```

### Metrics

```bash
# JMX metrics (if enabled)
jconsole localhost:9010

# Or use Prometheus JMX exporter
# Add to JAVA_OPTS:
-javaagent:/path/to/jmx_prometheus_javaagent.jar=9404:config.yaml
```

### Log Aggregation

```bash
# Forward logs to external system (e.g., ELK stack)
# Configure in systemd service or Docker logging driver

# Docker with syslog
docker run -d --log-driver syslog --log-opt syslog-address=tcp://logstash:5000 adventure:latest
```

---

## Next Steps

1. **Configure Backups:** Set up automated backups of `saves/` directory
2. **Set Up Monitoring:** Integrate with Prometheus, Grafana, or DataDog
3. **Enable Auto-Scaling:** Use Kubernetes HPA or cloud auto-scaling groups
4. **Implement CDN:** Use CloudFlare or AWS CloudFront for static assets
5. **Database Migration:** Move from JSON to PostgreSQL for production scale

---

## Support & Contact

- **Issue Tracker:** [GitHub Issues](https://github.com/your-org/adventure/issues)
- **Documentation:** `docs/` directory
- **Operator Runbook:** `docs/operator_runbook.md`

---

**End of DEPLOYMENT.md**
