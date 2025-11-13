# Deployment Files

This directory contains all deployment-related files for !Adventure.

## Files

- **`deploy.ps1`** - Windows PowerShell deployment script
- **`deploy.sh`** - Linux/macOS Bash deployment script
- **`Dockerfile`** - Multi-stage Docker image definition
- **`docker-compose.yml`** - Docker Compose configuration
- **`.env.example`** - Environment variable template
- **`DEPLOYMENT.md`** - Comprehensive deployment guide

## Quick Start

### Windows
```powershell
.\deployment\deploy.ps1
```

### Linux/macOS
```bash
./deployment/deploy.sh
```

### Docker
```bash
docker-compose -f deployment/docker-compose.yml up -d
```

## Full Documentation

See **`DEPLOYMENT.md`** for complete deployment instructions including:
- Cross-platform deployment (Windows, Linux, macOS)
- Docker and Kubernetes deployment
- Cloud deployment (AWS, Azure, GCP)
- Configuration and tuning
- Troubleshooting

---

**Back to root:** [Main README](../README.md) | [Build Guide](../BUILD.md)
