# Copilot Instructions for !Adventure Codebase

## Project Overview
- Modular, text-based multiplayer RPG with procedural world generation, dynamic societies, and emergent storytelling
- **Java 21** project using Maven, JUnit 5, Jackson for JSON serialization
- Package: `org.adventure.*` with modules: `world`, `region`, `character`, `items`, `crafting`
- Main entry: `org.adventure.Game` (ASCII world viewer prototype with CLI args)
- All design/architecture docs in `docs/` are the **primary source of truth**

## Key Directories & Files
- `src/main/java/org/adventure/` — core Java source (world gen, regions, characters, items, crafting)
  - `Game.java` — main entry point with world viewer and interactive mode
  - `world/` — WorldGen, Plate, Biome, River, RegionalFeature classes
  - `region/` — Region, ResourceNode, RegionSimulator (tick-based simulation)
  - `character/` — Character, NPC, Trait, Skill, Race classes
  - `items/` — Item, ItemPrototype, ItemCategory, ItemRarity
  - `crafting/` — CraftingSystem, CraftingRecipe, CraftingProficiency
- `src/test/java/org/adventure/` — JUnit 5 tests (146 tests passing as of Phase 1.4)
- `docs/` — design docs, specs, operator guides (**source of truth**)
  - `design_decisions.md` — canonical architecture decisions
  - `specs_summary.md` — defaults, formulas, tick rates, event decay, mod sandbox caps
  - `TO_FIX.md` — implementation tracker (42 items complete ✅)
  - `BUILD.md` — comprehensive build guide, phases, quality gates, commands
- `doc-src/` — **per-file documentation mirror** (markdown docs for every Java class/test)
  - Structure mirrors `src/`: `doc-src/main/java/org/adventure/<Class>.md`, `doc-src/test/java/org/adventure/<TestClass>.md`
  - Use these for detailed API docs, design rationale, and implementation notes for specific classes
  - 84+ markdown files documenting classes like `Game.md`, `WorldGen.md`, `Character.md`, etc.
- `pom.xml` — Maven config (Java 21, JUnit 5.9.3, Jackson 2.15.2)

## Developer Workflow
1. **Pick a task** from `docs/TO_FIX.md` or `docs/BUILD.md` (current phase: MVP Phase 1)
2. **Branch naming:** `feature/<short-description>`
3. **Implementation:** Add code to `src/main/java/org/adventure/<module>/`, tests to `src/test/java/`
4. **Testing requirements:**
   - Add deterministic-seed tests for procedural generation (explicit seed, checksum equality)
   - Coverage: 70%+ for core, 85%+ for persistence/critical logic
   - All tests must pass before PR: `.\maven\mvn\bin\mvn.cmd test`
5. **Documentation:** Update `docs/design_decisions.md` for design changes, link affected files
6. **Quality gates:** See `BUILD.md` for phase-specific gates (determinism, caps, performance)

## Build & Run Commands
```powershell
# Clean build with tests
.\maven\mvn\bin\mvn.cmd clean test

# Package JAR (skip tests for speed)
.\maven\mvn\bin\mvn.cmd -DskipTests=true package

# Run ASCII world viewer (main entry point)
.\maven\mvn\bin\mvn.cmd exec:java -Dexec.args="--width 60 --height 25 --seed 12345"

# Or run packaged JAR directly
java -cp target\adventure-0.1.0-SNAPSHOT.jar org.adventure.Game --interactive

# Run specific test class
.\maven\mvn\bin\mvn.cmd test -Dtest=WorldGenTest

# Run all determinism tests
.\maven\mvn\bin\mvn.cmd test -Dtest=*DeterminismTest
```

## Project-Specific Patterns & Requirements

### Deterministic Generation (Critical)
- **All procedural generators MUST accept explicit seed:** Use `Random` with seed, never `Math.random()`
- **Checksum tests required:** Generate twice with same seed → compare checksums (see `WorldGenTest.deterministicGenerationProducesSameChecksum()`)
- **Example pattern:**
  ```java
  public void generate(long seed) {
      Random rng = new Random(seed);
      // Use rng for all random operations
  }
  ```

### Data Model Conventions
- All persisted objects include: `type` (module/ObjectName), `schemaVersion` (int), `lastProcessedTick` (where applicable)
- Builder pattern for complex objects (see `Item.Builder`, `ItemPrototype.Builder`)
- Immutable where possible (use `final` fields, defensive copies)

### Tick-Based Simulation
- Default tick = 1 second (configurable)
- Active regions: `activeTickRateMultiplier = 1.0`
- Background regions: `backgroundTickRateMultiplier = 1/60`
- All simulation objects track `lastProcessedTick` for resynchronization

### Testing Requirements
- **Unit tests:** JUnit 5 + Mockito, 70% coverage minimum
- **Integration tests:** Use TestContainers for database/service mocking
- **Determinism tests:** Must validate reproducibility with explicit seeds
- **Test naming:** `testFeatureDescription()` or `testFeature_condition_expectedOutcome()`
- **See existing tests:** `WorldGenTest`, `BiomeTest`, `RiverTest`, `RegionSimulatorTest`, `CharacterTest`, `ItemTest`

### Code Style
- Google Java Style Guide, max line length 100
- Package structure: `org.adventure.<module>.<Class>`
- Commit format: `<type>(<scope>): <subject>` (e.g., `feat(worldgen): add plate simulation`)

## Key Design Decisions (from docs/design_decisions.md)
- **Determinism:** All generation reproducible from seed + params
- **Simulation:** Tick-driven, 1s default tick, active/background multipliers
- **Ownership:** Singular (not fractional), with separate access permissions
- **Persistence:** JSON format (human-readable), atomic writes, backup rotation (N=5)
- **Modding:** Data-only (JSON/YAML) for MVP, sandboxed scripting post-MVP

## Canonical Defaults (from docs/specs_summary.md)
- Tick: 1s, active=1.0, background=1/60
- Tax: rate=0.05 (5%), cadence=weekly, grace=14 days, seizure=21 days
- Events: decay k=0.8, maxHops=6, maxActiveStories=50/region, maxActiveEvents=20/region
- Crafting: Tiers (Novice 0-99xp, Apprentice 100-299, Journeyman 300-599, Expert 600-999, Master 1000+)
- Mana: `maxMana = baseMana + (castingStat * manaPerStat)`, defaults baseMana=10, manaPerStat=2

## Important Gotchas
- **`doc-src/` is for documentation only** — it mirrors `src/` structure with markdown files, not code
- **Always update `docs/design_decisions.md`** when changing architecture
- **Check `BUILD.md` quality gates** before marking phase complete
- **Run determinism tests** after any generation code change
- **Maven wrapper:** Use `.\maven\mvn\bin\mvn.cmd` on Windows, `./maven/mvn/bin/mvn` on Linux/macOS
- **Per-class docs:** Check `doc-src/main/java/org/adventure/<module>/<Class>.md` for detailed API docs
---

**For detailed phase information, quality gates, and troubleshooting, see `BUILD.md`. For design rationale, see `docs/design_decisions.md` and `docs/specs_summary.md`.**
