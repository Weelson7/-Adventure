# !Adventure — Quick Reference Card

**Version:** 0.1.0-SNAPSHOT  
**Java:** 21 LTS  
**Phase:** MVP Phase 1 Complete ✅

---

## Essential Commands

### Build & Package

```bash
# Windows
.\deployment\deploy.ps1                   # Build + test + package
.\deployment\deploy.ps1 -SkipTests        # Fast build (skip tests)
.\deployment\deploy.ps1 -BuildDocker      # Build + Docker image
.\maven\mvn\bin\mvn.cmd clean package     # Manual Maven build

# Linux/macOS
./deployment/deploy.sh                    # Build + test + package
./deployment/deploy.sh --skip-tests       # Fast build (skip tests)
./deployment/deploy.sh --build-docker     # Build + Docker image
./maven/mvn/bin/mvn clean package         # Manual Maven build
```

### Run the Game

```bash
# Server mode (multiplayer)
java -jar target/adventure-0.1.0-SNAPSHOT.jar --server --port 8080

# Interactive client
java -jar target/adventure-0.1.0-SNAPSHOT.jar --interactive

# World viewer (ASCII art)
java -jar target/adventure-0.1.0-SNAPSHOT.jar --width 60 --height 25 --seed 12345
```

### Testing

```bash
# Windows
.\maven\mvn\bin\mvn.cmd test              # Run all tests (534 tests)
.\maven\mvn\bin\mvn.cmd verify            # Test + coverage check
.\maven\mvn\bin\mvn.cmd jacoco:report     # Generate coverage report

# Linux/macOS
./maven/mvn/bin/mvn test                  # Run all tests (534 tests)
./maven/mvn/bin/mvn verify                # Test + coverage check
./maven/mvn/bin/mvn jacoco:report         # Generate coverage report
```

### Docker

```bash
# Docker Compose (recommended)
docker-compose -f deployment/docker-compose.yml up -d adventure-server     # Start production server
docker-compose -f deployment/docker-compose.yml logs -f adventure-server   # View logs
docker-compose -f deployment/docker-compose.yml down                       # Stop server

# Manual Docker
docker build -f deployment/Dockerfile -t adventure:latest .  # Build image
docker run -d -p 8080:8080 \              # Run container
  -v $(pwd)/saves:/app/saves \
  adventure:latest
```

---

## Key Files

| File | Purpose |
|------|---------|
| `BUILD.md` | Build guide, phases, quality gates |
| `deployment/DEPLOYMENT.md` | Deployment guide (all platforms) |
| `docs/design_decisions.md` | Canonical architecture decisions |
| `docs/specs_summary.md` | Defaults, formulas, tick rates |
| `docs/TO_FIX.md` | Implementation tracker (42/42 complete ✅) |
| `pom.xml` | Maven configuration (dependencies, plugins) |
| `deployment/deploy.ps1` / `deploy.sh` | Cross-platform deployment scripts |
| `deployment/Dockerfile` | Docker image definition |
| `deployment/docker-compose.yml` | Docker Compose configuration |

---

## Project Structure

```
!Adventure/
├── src/
│   ├── main/java/org/adventure/    # Java source
│   │   ├── Game.java                # Main entry point
│   │   ├── world/                   # World generation
│   │   ├── region/                  # Region simulation
│   │   ├── character/               # Characters & NPCs
│   │   ├── items/                   # Items & prototypes
│   │   ├── crafting/                # Crafting system
│   │   ├── structures/              # Structures & ownership
│   │   ├── societies/               # Clans & diplomacy
│   │   ├── stories/                 # Stories & events
│   │   ├── persistence/             # Save/load
│   │   └── network/                 # Multiplayer
│   └── test/java/org/adventure/    # JUnit 5 tests
├── docs/                            # Design documentation
├── doc-src/                         # Per-file documentation
├── archive/                         # Phase summaries
├── maven/                           # Maven wrapper
└── target/                          # Build output
```

---

## Quality Metrics

- **Tests:** 534 (all passing ✅)
- **Coverage:** 70%+ (JaCoCo enforced)
- **Platforms:** Windows, Linux, macOS
- **Docker:** Multi-stage build, 350 MB image
- **CI/CD:** GitHub Actions (multi-platform, nightly tests)

---

## Common Workflows

### Local Development

```bash
# 1. Make code changes
# 2. Run tests
.\maven\mvn\bin\mvn.cmd test  # Windows
./maven/mvn/bin/mvn test      # Linux/macOS

# 3. Build and run
.\deployment\deploy.ps1 -SkipTests -RunServer  # Windows
./deployment/deploy.sh --skip-tests --run-server  # Linux/macOS
```

### Docker Development

```bash
# 1. Make code changes
# 2. Rebuild Docker image
docker-compose -f deployment/docker-compose.yml up -d --build

# 3. View logs
docker-compose -f deployment/docker-compose.yml logs -f adventure-server

# 4. Test changes
java -jar target/adventure-0.1.0-SNAPSHOT.jar --interactive
```

### Production Deployment

```bash
# 1. Build release JAR
mvn clean package -DskipTests=true

# 2. Upload to server
scp target/adventure-0.1.0-SNAPSHOT.jar user@server:~/

# 3. SSH into server and run
ssh user@server
java -Xmx8g -Xms2g -jar adventure-0.1.0-SNAPSHOT.jar --server --port 8080

# Or use systemd service (see DEPLOYMENT.md)
```

---

## Troubleshooting

### Build Fails

```bash
# Check Java version (must be 21.0.x)
java -version

# Clean build cache
.\maven\mvn\bin\mvn.cmd clean  # Windows
./maven/mvn/bin/mvn clean      # Linux/macOS
```

### Tests Fail

```bash
# Run specific test
.\maven\mvn\bin\mvn.cmd test -Dtest=WorldGenTest  # Windows
./maven/mvn/bin/mvn test -Dtest=WorldGenTest      # Linux/macOS

# Check logs
cat target/surefire-reports/org.adventure.WorldGenTest.txt
```

### Server Won't Start

```bash
# Check if port is in use
netstat -ano | findstr :8080  # Windows
lsof -i :8080                 # Linux/macOS

# Use different port
java -jar target/adventure-0.1.0-SNAPSHOT.jar --server --port 9090
```

---

## Support

- **Documentation:** `docs/` directory
- **Build Guide:** `BUILD.md`
- **Deployment Guide:** `deployment/DEPLOYMENT.md`
- **GitHub Issues:** (link to repository issues)

---

**For detailed information, see BUILD.md and deployment/DEPLOYMENT.md**
