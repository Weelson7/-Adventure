# !Adventure — Living World RPG (documentation + developer guide)

Welcome to !Adventure — a modular, text-based, multiplayer RPG with procedural world generation, dynamic societies, and emergent storytelling. This repository contains the design docs, reference implementations, and tools to build and run the project.

This README gives a concise orientation for contributors and operators: where to find design docs, how to run the minimal Java app, development workflows, testing guidance, and where to triage outstanding design decisions.

## Quick links

- Docs: `docs/` (detailed design documents and decisions)
- Build Guide: `BUILD.md` (phases, quality gates, build commands)
- Deployment Guide: `deployment/DEPLOYMENT.md` (Windows, Linux, Docker, cloud platforms)
- High-level tracker: `docs/TO_FIX.md` (implementation tracker)
- Canonical decisions: `docs/design_decisions.md`
- Specs & defaults: `docs/specs_summary.md`
- Open questions / triage: `docs/open_questions.md`
- Operator runbook: `docs/operator_runbook.md`

## Repo layout

- `src/` — Java source (prototype entry points)
- `lib/` — third-party jars or libraries used by local dev builds
- `docs/` — all design documents and operator guides (primary source of truth)
- `README.md` — this file
- `App.java` — small runnable entry (example / prototype)

If you need a full IDE layout for VS Code, use the existing `.vscode` settings or generate one for your environment.

## Running the quick prototype (Java)

This repo contains a fully functional game with world generation, region simulation, characters, items, crafting, societies, stories, persistence, and multiplayer networking. The project includes executable JAR packaging for easy deployment.

### Quick Start (Recommended)

#### Windows (PowerShell)
```powershell
# One-command deployment (build, test, package)
.\deployment\deploy.ps1

# Run the game
java -jar target\adventure-0.1.0-SNAPSHOT.jar --interactive
```

#### Linux/macOS (Bash)
```bash
# One-command deployment (build, test, package)
./deployment/deploy.sh

# Run the game
java -jar target/adventure-0.1.0-SNAPSHOT.jar --interactive
```

### Manual Build (Maven)

Example: compile & run with Maven (Windows PowerShell):

```powershell
# Build and test
.\maven\mvn\bin\mvn.cmd clean package

# Run server mode
java -jar target\adventure-0.1.0-SNAPSHOT.jar --server --port 8080

# Run interactive client
java -jar target\adventure-0.1.0-SNAPSHOT.jar --interactive

# Run world viewer (ASCII art)
java -jar target\adventure-0.1.0-SNAPSHOT.jar --width 60 --height 25 --seed 12345
```

For Linux/macOS, use `./maven/mvn/bin/mvn` instead.

### Docker Deployment

```bash
# Build and run with Docker Compose
docker-compose -f deployment/docker-compose.yml up -d adventure-server

# Or build Docker image manually
docker build -f deployment/Dockerfile -t adventure:latest .
docker run -d -p 8080:8080 adventure:latest
```

### Full Deployment Guide

See **`deployment/DEPLOYMENT.md`** for comprehensive deployment instructions including:
- Cross-platform deployment (Windows, Linux, macOS)
- Docker and Kubernetes deployment
- Cloud deployment (AWS, Azure, GCP)
- Configuration and tuning
- Troubleshooting

## Documentation structure (high-level)

All design artifacts live in `docs/`. Key files:

- `docs/design_decisions.md` — centralized, authoritative decisions
- `docs/specs_summary.md` — actionable defaults and short contracts (tick rates, tax defaults, event decay, mod sandbox caps)
- `docs/open_questions.md` — triaged open questions with owners
- `docs/TO_FIX.md` — implementation tracker
- Feature docs (each gives specification, data models, algorithms, edge cases):
	- `world_generation.md`
	- `biomes_geography.md`
	- `structures_ownership.md`
	- `objects_crafting_legacy.md`
	- `characters_stats_traits_skills.md`
	- `societies_clans_kingdoms.md`
	- `stories_events.md`
	- `economy_resources.md`
	- `persistence_versioning.md`

If you change any design, update `docs/design_decisions.md` and add a short note linking to affected files.

## Development workflow

1. Pick a feature or issue from `docs/TO_FIX.md` or `docs/open_questions.md`.
2. Create a branch `feature/<short-description>`.
3. Implement the prototype in `src/` and add or update docs in `docs/`.
4. Add tests (deterministic-seed tests for procedural generation are required) and run locally.
5. Open a PR with design notes and a short playtest plan.

Use the `docs/TEMPLATE.md` when adding or editing feature docs to keep format consistent.

## Testing guidance

- Deterministic-seed tests: every procedural generator must accept an explicit seed and pass a checksum equality test when run twice with the same seed.
- Recommended test stack: Java + JUnit 5 + Mockito for unit tests. Integration tests may use TestContainers.
- Coverage goals: 70% unit coverage for core modules, 85%+ for persistence and critical logic.

I can add a test scaffold and CI workflow on request.

## Operator notes

See `docs/operator_runbook.md` for recovery, migration, and backup procedures. `docs/specs_summary.md` contains canonical defaults for operators.

## Contributing

We welcome contributions. Please:

1. Open an issue for non-trivial proposals before large changes.
2. Follow the repo's coding style and document design changes in `docs/design_decisions.md`.
3. Include deterministic tests for procedural systems when applicable.

## Contact & governance

For now, file issues and PRs on the repository. If you'd like, we can add a CONTRIBUTORS.md and a code of conduct.

## Next steps I can help with

- Add a Java test scaffold + CI
- Inline canonical defaults into feature docs (structures, economy)
- Add a developer quickstart script to automate build/run/test

Tell me which of the above you'd like me to do next and I'll apply patches.

## Recent documentation & operator additions

I added a few pragmatic documents and defaults to unblock implementation and operations. See:

- `docs/specs_summary.md` — canonical defaults and short contracts (tick defaults, event decay, tax defaults, mod sandbox caps, persistence recommendations).
- `docs/operator_runbook.md` — step-by-step operator playbook for checksum validation, backup restore, chunk regeneration, streaming migrations, and rollback (PowerShell examples included).
- `docs/open_questions.md` — triaged open questions with owners/status and links to the canonical specs.
- `docs/design_decisions.md` — now contains a "Canonical Defaults" section mirroring `specs_summary.md` for implementer visibility.
- `docs/game_parameters_setup.md` — updated with example presets (Classic Fantasy, High Lethality, Resource Rich, Magic Overload, Peaceful Exploration).

If you'd like I can also add a CI/test scaffold or inline the defaults into specific feature docs. Ask me to "add CI scaffold" or "inline defaults" and I'll apply the changes.
