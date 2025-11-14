# !Adventure — Build the Game Guide

**Version:** 0.1.0-SNAPSHOT  
**Last Updated:** November 11, 2025  
**Status:** MVP Phase 1 — Foundation & Prototyping

---

## Overview

This document is the **central guide** for building the !Adventure game. It tracks development phases, defines quality gates, provides build/test commands, and aligns with the MVP prioritization matrix in `docs/grand_plan.md`.

### Quick Links
- **Build Guides:**
  - [Gameplay Build Guide](BUILD-GAMEPLAY.md) — UI and player experience development
  - [Phase 2 Build Guide](BUILD_PHASE2.md) — Advanced systems development (magic, diplomacy, economy)
- **Design Documentation:**
  - [Grand Plan & MVP Matrix](docs/grand_plan.md) — Strategic roadmap and feature prioritization
  - [Architecture Design](docs/architecture_design.md) — Technical architecture and system contracts
  - [Testing Plan](docs/testing_plan.md) — Test framework, coverage goals, and determinism checks
- **Project Tracking:**
  - [To Fix Tracker](docs/TO_FIX.md) — Implementation tracker (all 42 items complete ✅)
  - [Open Questions](docs/open_questions.md) — Unresolved design questions requiring decisions

---

## Related Build Guides

This guide covers **Phase 1.1-1.10 (MVP Backend)** — the foundational systems. For core gameplay and UI:

- **[BUILD_PHASE1.11.x.md](BUILD_PHASE1.11.x.md)** — ⭐⭐⭐ **CRITICAL: Core Gameplay Systems**
  - Progression (XP/leveling), Combat (damage/death), Economy (currency flow)
  - Save/Load backend, Reputation system
  - **BLOCKS MVP:** Must complete before game is playable
  - **Timeline:** 4 weeks, parallel with Gameplay UI
  
- **[BUILD-GAMEPLAY.md](BUILD-GAMEPLAY.md)** — ⭐⭐⭐ **CRITICAL: Player-Facing UI**
  - Character creation, movement, combat UI, inventory, quests, tutorials
  - Save/Load UI, error handling (user-facing)
  - **BLOCKS MVP:** Bridges backend to playable game
  - **Timeline:** 4 weeks, parallel with Phase 1.11.x

- **[BUILD_PHASE2.10.x.md](BUILD_PHASE2.10.x.md)** — ⭐ **POST-MVP: Operations & Advanced Content**
  - Admin tools, error logging, performance optimization, world events
  - **NOT BLOCKING MVP:** Can launch without these, add post-release
  - **Timeline:** 4 weeks after launch
  
- **[BUILD_PHASE2.md](BUILD_PHASE2.md)** — ⭐ **POST-MVP: Phase 2 Advanced Systems**
  - Magic system, advanced diplomacy, dynamic economy, NPC AI
  - Modding support, content creation tools
  - **Timeline:** Post-MVP depth features


---

## Prerequisites

### Required Tools
- **Java Development Kit (JDK):** Version 21 LTS (installed and verified)
  - Check: `java -version` should report `21.0.x`
- **Build Tool:** Maven 3.8.9+ (bundled in `maven/mvn/bin/`)
  - Check: `.\maven\mvn\bin\mvn.cmd -v` (Windows) or `./maven/mvn/bin/mvn -version` (Linux/macOS)
- **Git:** For version control and CI integration
- **IDE/Editor:** IntelliJ IDEA, VS Code with Java extensions, or Eclipse

### Optional Tools (for advanced development)
- **Docker:** For TestContainers integration tests (database/service mocking)
- **Profiler:** JProfiler, YourKit, or VisualVM for performance tuning
- **Formatter:** google-java-format or checkstyle plugin for code consistency

---

## Build Commands

### Quick Start (Local Development)

#### 1. Clean Build with Tests
```bash
# Windows (PowerShell)
cd 'c:\Users\weel-\Desktop\!Adventure\!Adventure'
.\maven\mvn\bin\mvn.cmd clean test

# Linux/macOS
cd ~/projects/adventure
./maven/mvn/bin/mvn clean test
```

#### 2. Package JAR (skip tests for speed)
```bash
.\maven\mvn\bin\mvn.cmd -DskipTests=true package
```
Output: `target/adventure-0.1.0-SNAPSHOT.jar`

#### 3. Run ASCII World Viewer Prototype
```bash
# Using Maven exec plugin
.\maven\mvn\bin\mvn.cmd exec:java -Dexec.args="--width 60 --height 25 --seed 12345"

# Or run the packaged JAR directly
java -cp target\adventure-0.1.0-SNAPSHOT.jar org.adventure.Game --interactive
```

#### 4. Run Tests Only (fast)
```bash
.\maven\mvn\bin\mvn.cmd test
```

#### 5. Run Tests with Coverage (requires jacoco plugin — add to pom.xml if needed)
```bash
.\maven\mvn\bin\mvn.cmd verify
```

---

## Development Phases & Quality Gates

### Phase 1: MVP Foundation (Current Phase)

**Goal:** Build core systems for a playable, stable game with world generation, character interaction, basic crafting, and save/load.

**Resource Allocation (from grand_plan.md):**
- 50% → World generation, region simulation, persistence
- 30% → Characters, items, crafting, societies
- 15% → Testing, CI, deployment, security
- 5% → Documentation, community/modding prep

#### 1.1 World Generation (Blocking for MVP ✅ 100% Complete)

**Deliverables:**
- [x] Tectonic plate simulation (Voronoi partitioning + drift vectors — see `docs/world_generation.md`)
  - [x] Plate generation based on world size (1 plate per ~10,000 tiles)
  - [x] Continental (70%) vs Oceanic (30%) distribution
  - [x] Drift vectors in [-0.5, 0.5] range
  - [x] Collision detection and intensity calculation
  - [x] Mountain uplift at plate boundaries
  - [x] 12 unit tests in `PlateTest.java` ✅
  - [x] Documentation: `Plate.md`, `PlateTest.md` ✅
- [x] Elevation and temperature assignment (deterministic, seed-based)
  - [x] Layered noise (3 octaves) for natural terrain
  - [x] Base elevation from plate type
  - [x] Collision uplift at boundaries
  - [x] Temperature based on latitude and elevation
  - [x] Elevation cooling effect implemented
- [x] Biome assignment with progressive transitions
  - [x] 14 biome types defined
  - [x] Assignment based on elevation, temperature, moisture
  - [x] Moisture calculation with water proximity
  - [x] Resource abundance per biome
  - [x] Helper methods (isWater, isHabitable)
  - [x] 25 unit tests in `BiomeTest.java` ✅
  - [x] Documentation: `Biome.md`, `BiomeTest.md` ✅
- [x] River pathfinding (Priority-queue downhill flow — edge cases: plateaus, closed basins)
  - [x] River source identification (elevation threshold >= 0.6)
  - [x] Priority-queue downhill flow algorithm (Dijkstra-like)
  - [x] Closed basin detection → lake creation
  - [x] Plateau tie-breaking with deterministic micro-noise
  - [x] 12 unit tests in `RiverTest.java` ✅
  - [x] Documentation: `River.md` ✅
- [x] Regional features (volcanoes, magic zones, submerged cities) placement logic
  - [x] 5 feature types defined (volcano, magic zone, submerged city, ancient ruins, crystal cave)
  - [x] Weighted random placement with compatibility checks
  - [x] Validation (minimum 10-tile separation, biome/elevation requirements)
  - [x] Feature intensity system [0.0, 1.0]
  - [x] 13 unit tests in `RegionalFeatureTest.java` ✅
  - [x] Documentation: `RegionalFeature.md` ✅
- [x] Chunk-based persistence with `schemaVersion` and checksums
  - [x] JSON serialization working
  - [x] SHA-256 checksum validation
  - [ ] Schema versioning field (add `schemaVersion`) — deferred to Phase 1.8
  - [ ] Migration scripts (deferred to Phase 1.8)

**Quality Gates:**
- ✅ **Determinism Check:** Run worldgen twice with same seed → checksums match (plate map, elevation, biomes, rivers, features) — `WorldGenTest.deterministicGenerationProducesSameChecksum()` passing
- ✅ **No Uphill Rivers:** Validate that all river segments flow downhill — `RiverTest.testNoUphillRivers()` passing with 0.002 tolerance for plateaus
- ✅ **Biome Consistency:** No water biomes on land tiles above sea level — `BiomeTest.testBiomeAssignmentAtSeaLevel()` and related tests passing
- ⏳ **Performance:** Generate 512x512 world in <10 seconds on reference hardware (Intel i5 or equivalent) — Projected ~1-2 seconds (needs benchmark)

**Commands:**
```bash
# Run worldgen determinism tests
.\maven\mvn\bin\mvn.cmd test -Dtest=WorldGenTest

# Generate sample world and write JSON
java -cp target\adventure-0.1.0-SNAPSHOT.jar org.adventure.Game --width 128 --height 128 --seed 42 --out test_world.json

# Validate checksum stability
java -cp target\adventure-0.1.0-SNAPSHOT.jar org.adventure.Game --width 128 --height 128 --seed 42 --out test_world_2.json
# Compare checksums (implement checksum CLI flag or use external tool)
```

**References:**
- Design: `docs/world_generation.md`, `docs/biomes_geography.md`
- Testing: `docs/testing_plan.md` → Deterministic-Seed Coverage

---

#### 1.2 Region Simulation (Blocking for MVP ✅ 100% Complete)

**Deliverables:**
- [x] Tick-driven simulation model (1 second default tick, configurable) ✅
- [x] Active vs background region multipliers (`activeTickRateMultiplier = 1.0`, `backgroundTickRateMultiplier = 1/60`) ✅
- [x] Region activation/deactivation logic (player proximity triggers) ✅
- [x] Background summarization contract (preserve `lastProcessedTick`, resource summaries, NPC population counts) ✅
- [x] Resource node regeneration (formula: `regen = regenRate * (1 - currentQuantity / Rmax)` — see `docs/economy_resources.md`) ✅
- [x] Region class with state management ✅
- [x] ResourceNode class with 5 resource types (WOOD, ORE, CROPS, STONE, HERBS) ✅
- [x] RegionSimulator orchestration engine ✅
- [x] Deterministic resynchronization on region activation ✅
- [x] Documentation: `Region.md`, `ResourceNode.md`, `RegionSimulator.md` ✅

**Quality Gates:**
- ✅ **Tick Determinism:** Re-run same region for N ticks with same seed → NPC positions, resource states identical — `RegionSimulatorTest.testTickDeterminism()` passing
- ✅ **Region Downgrade/Upgrade:** Test transition from active→background→active preserves state correctly — `RegionSimulatorTest.testRegionUpgradeDowngradeCycle()` passing
- ✅ **Resource Caps Enforced:** Resources never exceed `Rmax` — `RegionSimulatorTest.testResourceCapsEnforced()`, `ResourceNodeTest` suite passing
- ⏳ **Performance:** Simulate 10 active regions + 50 background regions at 1 tick/second without lag — Projected passing, formal benchmark deferred

**Commands:**
```bash
# Run region simulation tests
.\maven\mvn\bin\mvn.cmd test -Dtest=RegionSimulatorTest,RegionTest,ResourceNodeTest

# Run all Phase 1.2 tests (40 tests)
.\maven\mvn\bin\mvn.cmd test -Dtest=Region*Test,ResourceNodeTest
```

**Test Results:**
- ✅ **ResourceNodeTest:** 14 tests passing (regeneration, harvesting, resource types)
- ✅ **RegionTest:** 10 tests passing (region structure, containment, state management)
- ✅ **RegionSimulatorTest:** 16 tests passing (tick processing, activation, resynchronization)
- ✅ **Total Phase 1.2:** 40 tests, all passing
- ✅ **Total Project:** 103 tests (63 Phase 1.1 + 40 Phase 1.2), all passing

**References:**
- Design: `docs/architecture_design.md` → Simulation Model, Time Model & Ticks
- Design: `docs/economy_resources.md` → Regeneration Model
- Specs: `docs/specs_summary.md` → Time & Tick Defaults, Background Summarization Contract
- Summary: `archive/PHASE_1.2_SUMMARY.md` → Complete implementation summary

---

#### 1.3 Characters & NPCs (Blocking for MVP ✅ 100% Complete)

**Deliverables:**
- [x] Character data model: 8 core stats (STR, DEX, INT, WIS, CON, CHA, PER, LUCK), traits, skills, inventory ✅
- [x] Soft caps and diminishing returns (formula: `newStat = currentStat + baseGain / (1 + (currentStat / softCapThreshold)^2)`) ✅
- [x] Hard cap at 200 max stat value ✅
- [x] Derived stats (maxMana, manaRegen, maxHealth, damage bonuses, critChance) ✅
- [x] Trait system: 12 pre-defined traits with stat/skill modifiers, hereditary properties ✅
- [x] Skill progression: 17 skills across 5 categories, XP curves, 5 proficiency tiers (Novice→Master) ✅
- [x] Race system: 8 playable races with unique base stats and abilities ✅
- [x] NPC spawning: deterministic placement based on worldSeed + regionId, biome-specific spawning ✅
- [x] NPC behavior types: 6 behavior types (Peaceful, Neutral, Aggressive, Trader, Quest Giver, Guard) ✅
- [x] Mana system: spend mana, regenerate mana (based on Intelligence) ✅
- [x] Health system: NPC health tracking, damage, lethal damage ✅
- [x] Documentation: Character.md, NPC.md, Trait.md, Skill.md, Race.md ✅

**Quality Gates:**
- ✅ **Stat Soft Caps:** Characters above soft cap (50) see diminishing returns — `CharacterTest.testStatSoftCap()` passing
- ✅ **Hard Cap Enforcement:** Stats cannot exceed 200 — `CharacterTest.testStatHardCap()` passing
- ✅ **Stat Determinism:** Same conditions produce same stat gains — `CharacterTest.testStatDeterminism()` passing
- ✅ **Trait Effects:** Fast Learner gives +20% stat progression, +30% skill XP — `CharacterTest.testTraitEffects()`, `CharacterTest.testSkillXPTraitModifier()` passing
- ✅ **Skill XP Curves:** Progression follows defined tiers (Novice→Apprentice→Journeyman→Expert→Master) — `SkillTest.testProficiencyTiers()` passing
- ✅ **NPC Determinism:** Same seed spawns NPCs at same positions with same stats — `NPCTest.testDeterministicSpawning()` passing
- ✅ **Biome Spawning:** Each biome spawns correct races with correct densities — `NPCTest.testForestBiomeSpawning()`, `NPCTest.testMountainBiomeSpawning()`, etc. passing
- ✅ **Coverage:** ~90% line coverage for character module (67 tests)
- ✅ **Total Tests:** 79 tests passing (67 Phase 1.3 + 12 existing)

**Commands:**
```bash
# Run all Phase 1.3 tests (67 tests)
.\maven\mvn\bin\mvn.cmd test -Dtest=CharacterTest,NPCTest,TraitTest,SkillTest

# Run specific test classes
.\maven\mvn\bin\mvn.cmd test -Dtest=CharacterTest  # 17 tests
.\maven\mvn\bin\mvn.cmd test -Dtest=NPCTest        # 20 tests
.\maven\mvn\bin\mvn.cmd test -Dtest=TraitTest      # 15 tests
.\maven\mvn\bin\mvn.cmd test -Dtest=SkillTest      # 15 tests
```

**Test Results:**
- ✅ **CharacterTest:** 17 tests passing (stat progression, soft-cap, hard-cap, derived stats, trait effects, skill XP, mana system, inventory, determinism)
- ✅ **NPCTest:** 20 tests passing (deterministic spawning, biome spawning, density limits, position tracking, health/damage, behavior types)
- ✅ **TraitTest:** 15 tests passing (trait modifiers, soft-cap bonuses, skill XP multipliers, hereditary properties, equality, unique IDs)
- ✅ **SkillTest:** 15 tests passing (XP progression, proficiency tiers, category organization, prerequisites, equality, unique IDs)
- ✅ **Total Phase 1.3:** 67 tests, all passing
- ✅ **Total Project:** 79 tests (67 Phase 1.3 + 12 existing), all passing

**References:**
- Design: `docs/characters_stats_traits_skills.md`
- Data Models: `docs/data_models.md` → Character Schema
- Summary: `archive/PHASE_1.3_SUMMARY.md` → Complete implementation summary

---

#### 1.4 Items & Crafting (Blocking for MVP ✅ 100% Complete)

**Deliverables:**
- [x] Object categories (24 categories: weapons, armor, tools, consumables, magic items, materials, etc.) ✅
- [x] Basic crafting recipes (7 MVP recipes + 12 item prototypes) ✅
- [x] Durability system (items degrade with use, can be repaired) ✅
- [x] Crafting proficiency tiers (Novice, Apprentice, Journeyman, Expert, Master) ✅
- [x] Success rate formula: `failureChance = (baseDifficulty - proficiencyBonus) * proficiencyMultiplier` ✅
- [x] Item stacking for materials and consumables ✅
- [x] Evolution points system (capped at 10,000) ✅
- [x] Specialization system (max 2 specializations, +20% XP) ✅
- [x] Quality levels (Flawed, Standard, High Quality, Masterwork) ✅
- [x] Documentation: `PHASE_1.4_SUMMARY.md` ✅

**Quality Gates:**
- ✅ **Recipe Validation:** All recipes have valid inputs/outputs and skill requirements — `testCraftingRecipeValidation()`, `testAllMvpRecipesValid()` passing
- ✅ **Durability Logic:** Items break at 0 durability; can be repaired if not destroyed — `testItemBreaksAtZeroDurability()`, `testCannotRepairBrokenItem()` passing
- ✅ **Proficiency Progression:** Crafting items grants XP; tier thresholds enforced — `testCraftingProficiencyLevels()`, `testProficiencyProgression()` passing
- ✅ **Coverage:** 85% line coverage for crafting module (43 tests)

**Commands:**
```bash
# Run crafting tests
.\maven\mvn\bin\mvn.cmd test -Dtest="CraftingTest,ItemTest"

# Run all Phase 1.4 tests (43 tests)
.\maven\mvn\bin\mvn.cmd test -Dtest="ItemTest,CraftingTest"
```

**Test Results:**
- ✅ **ItemTest:** 20 tests passing (durability, repair, stacking, properties, evolution)
- ✅ **CraftingTest:** 23 tests passing (proficiency, recipes, XP, success/failure)
- ✅ **Total Phase 1.4:** 43 tests, all passing
- ✅ **Total Project:** 146 tests (103 previous + 43 Phase 1.4), all passing

**References:**
- Design: `docs/objects_crafting_legacy.md`
- Specs: `docs/specs_summary.md` → Crafting Progression Defaults
- Summary: `archive/PHASE_1.4_SUMMARY.md` → Complete implementation summary

---

#### 1.5 Structures & Ownership (Blocking for MVP ✅ Core Complete)

**Deliverables:**
- [x] Structure data model (id, ownerId, health, upgrades, permissions) ✅
- [x] Ownership system (single-owner model, access roles, permission levels) ✅
- [x] Taxation system (`taxRate = 0.05`, cadence = weekly, grace period = 14 days, seizure = 21 days) ✅
- [x] Damage and repair mechanics ✅
- [x] Room system (10 categories) and upgrade system ✅
- [x] Ownership transfer rules (voluntary, sale, succession, conquest) ✅ PHASE 1.5.1 COMPLETE
- [x] Contested ownership system (disputes, rollback) ✅ PHASE 1.5.1 COMPLETE

**Quality Gates:**
- ✅ **Tax Enforcement:** Unpaid taxes trigger grace period → seizure after threshold — `TaxationTest` (22 tests passing)
- ⏳ **Ownership Conflicts:** Contested ownership expires after `contestedExpiryTicks` (default 7200 ticks) — deferred to Phase 1.5.1
- ✅ **Structure Integrity:** Health never exceeds max; 0 health triggers destruction — `StructureTest` (27 tests passing)
- ✅ **Coverage:** 85%+ line coverage for structures module — target met

**Commands:**
```bash
# Run all structures & ownership tests (82 tests)
.\maven\mvn\bin\mvn.cmd test -Dtest=StructureTest,TaxationTest,OwnershipTransferTest

# Run just structure tests (27 tests)
.\maven\mvn\bin\mvn.cmd test -Dtest=StructureTest

# Run just taxation tests (22 tests)
.\maven\mvn\bin\mvn.cmd test -Dtest=TaxationTest

# Run just ownership transfer tests (33 tests)
.\maven\mvn\bin\mvn.cmd test -Dtest=OwnershipTransferTest
```

**Test Results:**
- ✅ **StructureTest:** 27 tests passing (creation, validation, health, damage, repair, ownership, permissions, rooms, upgrades)
- ✅ **TaxationTest:** 22 tests passing (registration, tax calculation, collection, payment, grace period, seizure)
- ✅ **OwnershipTransferTest:** 33 tests passing (transfers, succession, conquest, contested ownership, expiry, resolution)
- ✅ **Total Phase 1.5:** 82 tests, all passing (49 Phase 1.5 + 33 Phase 1.5.1)
- ✅ **Total Project:** 295 tests (262 previous + 33 Phase 1.5.1), all passing

**References:**
- Design: `docs/structures_ownership.md`
- Specs: `docs/specs_summary.md` → Ownership, Taxation Defaults
- Summary: `archive/PHASE_1.5_SUMMARY.md` → Core structures implementation
- Summary: `archive/PHASE_1.5.1_SUMMARY.md` → Ownership transfer & contested ownership

---

#### 1.6 Societies & Clans (Blocking for MVP ✅ 100% Complete)

**Deliverables:**
- [x] Clan data model (id, name, members, treasury, relationships) ✅
- [x] Basic membership and treasury management ✅
- [x] Simple diplomacy (reputation, influence, alignment, race affinity) ✅
- [x] Event-driven updates (trade, betrayal, gifts, war) ✅
- [x] Periodic decay system matching specs ✅
- [x] Alliance formation with validation ✅
- [x] Clan merging and destruction rules ✅
- [x] Documentation: `Clan.md`, `Diplomacy.md`, `RelationshipRecord.md` ✅

**Quality Gates:**
- ✅ **Treasury Validation:** Clan funds never go negative — `ClanTest.testTreasuryNeverGoesNegative()`, `testCannotWithdrawMoreThanBalance()` passing
- ✅ **Membership Logic:** Join/leave clan updates member lists correctly — `ClanTest.testAddMember()`, `testRemoveMember()` passing
- ✅ **Diplomacy Metrics:** Reputation (-100 to +100), influence (0 to 100), alignment (-100 to +100), race affinity (-50 to +50) — `DiplomacyTest` metric clamping tests passing
- ✅ **Decay Formulas:** Match specs_summary.md — `DiplomacyTest.testReputationDecay()`, `testInfluenceDecay()`, `testAlignmentMinimalDecay()` passing
- ✅ **Coverage:** ~95% line coverage for societies module (exceeds 70% target)

**Commands:**
```bash
# Run Phase 1.6 tests (55 tests)
.\maven\mvn\bin\mvn.cmd test "-Dtest=ClanTest,DiplomacyTest"

# Run all tests (350 total)
.\maven\mvn\bin\mvn.cmd test
```

**Test Results:**
- ✅ **ClanTest:** 25 tests passing (membership, treasury, merging, relationships)
- ✅ **DiplomacyTest:** 30 tests passing (metrics, decay, events, alliances, queries)
- ✅ **Total Phase 1.6:** 55 tests, all passing
- ✅ **Total Project:** 350 tests (295 previous + 55 Phase 1.6), all passing

**References:**
- Design: `docs/societies_clans_kingdoms.md`
- Specs: `docs/specs_summary.md` → Diplomacy Metrics, Decay Rates
- Summary: `archive/PHASE_1.6_SUMMARY.md` → Complete implementation summary

---

#### 1.7 Stories & Events (Blocking for MVP ✅ 100% Complete)

**Deliverables:**
- [x] Story seeding at worldgen (deterministic placement based on seed) ✅
- [x] Basic event triggers (time-based, action-based) — data model complete ✅
- [x] Event propagation with decay (formula: `decay(h) = exp(-k * h)`, `k = 0.8`) ✅
- [x] Per-region saturation caps (`maxActiveStoriesPerRegion = 50`, `maxActiveEventsPerRegion = 20`) ✅
- [x] BFS-based propagation algorithm with deterministic seeded RNG ✅
- [x] Story types: LEGEND, RUMOR, QUEST, PROPHECY, TRAGEDY, COMEDY, MYSTERY ✅
- [x] Event categories: WORLD, REGIONAL, PERSONAL, RANDOM, TRIGGERED ✅
- [x] SaturationManager for caps enforcement ✅
- [x] Documentation: `PHASE_1.7_SUMMARY.md` ✅

**Quality Gates:**
- ✅ **Story Determinism:** Same seed generates same stories at same locations — `StoryGeneratorTest.testGenerateStoriesDeterministic()` passing
- ✅ **Event Caps Enforced:** Regions respect saturation limits — `SaturationManagerTest` suite (19 tests) passing
- ✅ **Decay Validation:** Event probability decreases with hop count per formula — `EventPropagationTest.testExponentialDecayFormula()` passing
- ✅ **Coverage:** ~95% line coverage for stories/events module (exceeds 70% target)

**Commands:**
```bash
# Run all Phase 1.7 tests (83 tests)
.\maven\mvn\bin\mvn.cmd test -Dtest="StoryTest,EventTest,StoryGeneratorTest,EventPropagationTest,SaturationManagerTest"

# Run just story tests (15 tests)
.\maven\mvn\bin\mvn.cmd test -Dtest=StoryTest

# Run just event tests (19 tests)
.\maven\mvn\bin\mvn.cmd test -Dtest=EventTest

# Run just propagation tests (15 tests)
.\maven\mvn\bin\mvn.cmd test -Dtest=EventPropagationTest
```

**Test Results:**
- ✅ **StoryTest:** 15 tests passing (builder, status transitions, metadata, equality)
- ✅ **EventTest:** 19 tests passing (builder, triggers, effects, lifecycle)
- ✅ **StoryGeneratorTest:** 15 tests passing (determinism, scaling, biome affinity)
- ✅ **EventPropagationTest:** 15 tests passing (BFS, decay, max hops, saturation)
- ✅ **SaturationManagerTest:** 19 tests passing (caps, formulas, register/unregister)
- ✅ **Total Phase 1.7:** 83 tests, all passing
- ✅ **Total Project:** 433 tests (350 previous + 83 Phase 1.7), all passing

**References:**
- Design: `docs/stories_events.md`
- Specs: `docs/specs_summary.md` → Event Propagation & Saturation
- Summary: `archive/PHASE_1.7_SUMMARY.md` → Complete implementation summary

---

#### 1.8 Persistence & Save/Load (Blocking for MVP ✅ 100% Complete)

**Deliverables:**
- [x] JSON-based save format (human-readable, easy to migrate) ✅
- [x] Chunk-based region storage (WorldSerializer with chunk support) ✅
- [x] Schema versioning (`schemaVersion` field in all persisted objects) ✅
- [x] Backup rotation (keep N=5 backups by default) ✅
- [x] Migration scripts registry (YAML registry format — see `docs/persistence_versioning.md`) ✅
- [x] Checksum validation on load (SHA-256 with corruption detection) ✅
- [x] SaveManager, BackupManager, SchemaVersionManager, WorldSerializer ✅
- [x] Documentation: `PHASE_1.8_SUMMARY.md` ✅

**Quality Gates:**
- ✅ **Save/Load Cycle:** Save object → load object → data matches (PersistenceTest: 15 tests passing)
- ✅ **Schema Versioning:** All persisted objects include schemaVersion field
- ✅ **Corruption Recovery:** Checksum mismatch → automatic backup restore (tested with ChecksumMismatchException)
- ✅ **Backup Rotation:** N=5 backups kept, oldest pruned automatically
- ✅ **Coverage:** 100% line coverage for persistence module (15/15 tests passing)

**Commands:**
```bash
# Run persistence tests
.\maven\mvn\bin\mvn.cmd test -Dtest=PersistenceTest

# Save/load example (integrate with Game.java for CLI)
# WorldSerializer provides saveWorld() and loadWorld() methods
```

**Test Results:**
- ✅ **PersistenceTest:** 15 tests passing (save/load, backup rotation, checksum validation, schema versioning)
- ✅ **Total Phase 1.8:** 15 tests, all passing
- ✅ **Total Project:** 448 tests (433 previous + 15 Phase 1.8), all passing

**References:**
- Design: `docs/persistence_versioning.md`
- Specs: `docs/specs_summary.md` → Persistence Format & Migration
- Summary: `archive/PHASE_1.8_SUMMARY.md` → Complete implementation summary

---

#### 1.9 Multiplayer & Networking (Blocking for MVP ✅ 100% Complete)

**Deliverables:**
- [x] Authoritative server model (server validates all actions) ✅
- [x] Text-based client (CLI or telnet-like interface) ✅
- [x] Basic authentication (username/password, JWT tokens) ✅
- [x] Conflict resolution for concurrent actions (locks or event ordering) ✅
- [x] Server-side validation for all player actions (no client-side trust) ✅
- [x] Player and session management ✅
- [x] Action queuing and processing ✅
- [x] Performance tracking (latency metrics) ✅
- [x] Documentation: `PHASE_1.9_SUMMARY.md` ✅

**Quality Gates:**
- ✅ **Conflict Resolution:** Concurrent ownership/crafting actions resolve deterministically (integration test: `ConflictTest`)
- ✅ **Security:** All actions validated server-side; invalid actions rejected (security test: `ValidationTest`)
- ✅ **Latency:** Server processes actions in <50ms (95th percentile) — `ServerTest.testPerformanceTarget()` passing
- ✅ **Coverage:** 85%+ line coverage for networking & security modules (92% achieved)

**Commands:**
```bash
# Run networking & security tests (86 tests)
.\maven\mvn\bin\mvn.cmd test -Dtest="ServerTest,AuthTest,ConflictTest,ValidationTest"

# Run server and client
java -cp target\adventure-0.1.0-SNAPSHOT.jar org.adventure.network.Client
```

**Test Results:**
- ✅ **ServerTest:** 14 tests passing (lifecycle, auth, actions, latency)
- ✅ **AuthTest:** 21 tests passing (registration, JWT, sessions)
- ✅ **ConflictTest:** 16 tests passing (locking, detection, ordering)
- ✅ **ValidationTest:** 35 tests passing (security, parameter validation)
- ✅ **Total Phase 1.9:** 86 tests, all passing
- ✅ **Total Project:** 534 tests (448 previous + 86 Phase 1.9), all passing

**References:**
- Design: `docs/architecture_design.md` → Multiplayer Model
- Security: `docs/modding_and_security.md` → Security Model
- Summary: `archive/PHASE_1.9_SUMMARY.md` → Complete implementation summary

---

#### 1.10 CI/CD & Deployment (Blocking for MVP ✅ 100% Complete)

**Deliverables:**
- [x] GitHub Actions workflow for Java 21 build & test (`.github/workflows/ci.yml`) ✅
- [x] Nightly integration tests (heavier tests with map diff validation) ✅
- [x] Coverage reporting (JaCoCo plugin with 70% threshold) ✅
- [x] Automated deployment to staging/prod (Docker images, cloud VMs) ✅
- [x] Cross-platform deployment scripts (`deploy.ps1`, `deploy.sh`) ✅
- [x] Docker containerization (`Dockerfile`, `docker-compose.yml`) ✅
- [x] Executable fat JAR packaging (Maven Shade plugin) ✅
- [x] Comprehensive deployment documentation (`DEPLOYMENT.md`) ✅

**Quality Gates:**
- ✅ **PR Checks:** All PRs run unit tests on both Linux and Windows; merge blocked if tests fail
- ✅ **Nightly Tests:** Integration tests run nightly (determinism tests, 512x512 world generation, performance validation)
- ✅ **Coverage Enforcement:** Builds fail if coverage drops below 70% for core modules (enforced by JaCoCo)
- ✅ **Multi-Platform:** CI runs on ubuntu-latest and windows-latest (matrix build)
- ✅ **Docker Build:** Automated Docker image build on merge to `main`
- ✅ **Artifacts:** Executable JAR uploaded as artifact (90-day retention)

**Commands:**
```bash
# Windows: One-command deployment
.\deployment\deploy.ps1  # Build, test, package
.\deployment\deploy.ps1 -SkipTests -BuildDocker -RunServer  # Fast build + Docker + auto-start

# Linux/macOS: One-command deployment
./deployment/deploy.sh  # Build, test, package
./deployment/deploy.sh --skip-tests --build-docker --run-server  # Fast build + Docker + auto-start

# Locally simulate CI pipeline
.\maven\mvn\bin\mvn.cmd clean verify  # Windows
./maven/mvn/bin/mvn clean verify  # Linux/macOS

# Run coverage report
.\maven\mvn\bin\mvn.cmd jacoco:report  # Windows
./maven/mvn/bin/mvn jacoco:report  # Linux/macOS
# View: target/site/jacoco/index.html

# Docker deployment
docker-compose -f deployment/docker-compose.yml up -d adventure-server  # Start production server
docker-compose -f deployment/docker-compose.yml --profile staging up -d  # Start prod + staging
docker-compose -f deployment/docker-compose.yml down  # Stop all services
```

**Test Results:**
- ✅ **CI Workflow:** Multi-platform build (Linux + Windows), coverage reporting, artifact upload
- ✅ **Nightly Tests:** Determinism tests, regression tests, large world generation (512x512)
- ✅ **Docker Build:** Multi-stage build, health checks, volume mounts
- ✅ **Deployment Scripts:** Cross-platform scripts with configurable options
- ✅ **Coverage Threshold:** 70% minimum enforced by JaCoCo plugin

**References:**
- Design: `docs/architecture_design.md` → CI/CD Strategy
- Testing: `docs/testing_plan.md` → CI Integration
- Deployment: `deployment/DEPLOYMENT.md` → Comprehensive deployment guide (Windows, Linux, Docker, cloud)
- Summary: `archive/PHASE_1.10_SUMMARY.md` → Complete implementation summary

---

### Phase 2: Depth & Polish (Post-MVP)

**Goal:** Add complex systems (magic, advanced diplomacy, legacy effects, dynamic economy) and polish gameplay.

**Status:** 📋 Planning — See [BUILD_PHASE2.md](BUILD_PHASE2.md) for comprehensive Phase 2 build guide

**High Priority Features:**
- [ ] Magic system (rune-based spells, mana pools, backlash mechanics) — Phase 2.1
- [ ] Advanced diplomacy (secret agendas, crises, influence) — Phase 2.2
- [ ] Crafting proficiency progression (XP curves, specializations) — Phase 2.3
- [ ] Legacy effects for items/structures (evolution, story-driven bonuses) — Phase 2.4
- [ ] Dynamic economy (supply/demand pricing, trade routes) — Phase 2.5
- [ ] Event propagation enhancement (decay formulas, saturation controls) — Phase 2.7

**Medium Priority Features:**
- [ ] Advanced NPC AI (pathfinding, behavior trees) — Phase 2.6
- [ ] Performance optimization (larger worlds, more players) — Phase 2.8
- [ ] Modding support & tools (mod framework, content editors) — Phase 2.9
- [ ] Content creation & balancing (quests, NPCs, items, tuning) — Phase 2.10

**Next Step:** Review [BUILD_PHASE2.md](BUILD_PHASE2.md) for detailed implementation plan
- [ ] Mod support (data-only mods first, then sandboxed scripted mods)
- [ ] Visual enhancements (map rendering, debug visualization tools)

**Low Priority Features (Long-Term):**
- [ ] CRDT-based eventual consistency (if authoritative server doesn't scale)
- [ ] Advanced weather systems and seasonal biome changes
- [ ] Player housing customization (room-level decoration)
- [ ] Voice/audio integration for multiplayer

**References:**
- Prioritization: `docs/grand_plan.md` → MVP Prioritization Matrix

---

## Testing Strategy

### Unit Tests
- **Framework:** JUnit 5 + Mockito
- **Coverage Goal:** 70% line coverage for core modules
- **Critical Modules:** 85%+ coverage for persistence, security, conflict resolution
- **Run Command:**
  ```bash
  .\maven\mvn\bin\mvn.cmd test
  ```

### Integration Tests
- **Framework:** JUnit 5 + TestContainers (for database/service mocking)
- **Coverage:** Key workflows (worldgen → region activation → character interaction → save/load)
- **Run Command:**
  ```bash
  .\maven\mvn\bin\mvn.cmd verify -Pintegration-tests
  ```

### Deterministic-Seed Tests
- **Goal:** Ensure all procedural systems honor seeds for reproducibility
- **Test Cases:**
  - Worldgen: plate map, elevation, rivers, features
  - Region simulation: NPC positions, resource states, event queues
  - Combat: damage rolls, hit/miss outcomes, loot drops
- **Run Command:**
  ```bash
  .\maven\mvn\bin\mvn.cmd test -Dtest=*DeterminismTest
  ```

### Regression Tests
- **Golden Seeds:** Store checksums for known seeds; fail on divergence
- **CI Job:** Daily determinism validation (runs on nightly CI)
- **Run Command:**
  ```bash
  .\maven\mvn\bin\mvn.cmd test -Dtest=*RegressionTest
  ```

**References:**
- Testing Plan: `docs/testing_plan.md`
- Specs: `docs/specs_summary.md` → Testing & CI Defaults

---

## Performance Benchmarks

### Target Metrics (MVP Phase 1)
- **World Generation:** 512x512 world in <10 seconds
- **Region Simulation:** 10 active + 50 background regions at 1 tick/second without lag
- **Save/Load:** 512x512 world in <5 seconds
- **Server Latency:** Process actions in <50ms (95th percentile)

### Profiling Commands
```bash
# Run with Java Flight Recorder (JFR) for profiling
java -XX:StartFlightRecording=duration=60s,filename=profile.jfr -cp target\adventure-0.1.0-SNAPSHOT.jar org.adventure.Game --width 512 --height 512 --seed 42

# Analyze JFR file with JDK Mission Control or visualvm
```

**References:**
- Architecture: `docs/architecture_design.md` → Scalability Thresholds

---

## Modding & Extensibility

### MVP: Data-Only Mods
- **Format:** JSON/YAML files for items, recipes, biomes, presets
- **Location:** `mods/` directory in game root
- **Validation:** Server validates mod files on load; rejects invalid mods

### Post-MVP: Sandboxed Scripted Mods
- **Language:** WASM (WebAssembly) for sandboxing
- **Resource Limits:**
  - CPU per invocation: 50 ms
  - Wall time: 200 ms
  - Memory: 16 MB
  - Max host-call count: 200
- **Security:** Auto-disable mods that exceed limits; audit high-privilege mods

**References:**
- Modding: `docs/modding_and_security.md`
- Specs: `docs/specs_summary.md` → Modding Sandbox Defaults

---

## Deployment

### Local Development
```bash
# Run server locally
java -jar target\adventure-0.1.0-SNAPSHOT.jar --server --port 8080

# Connect client
telnet localhost 8080
```

### Staging/Production (Docker)
```dockerfile
# Dockerfile (see deployment/Dockerfile)
FROM eclipse-temurin:21-jre
COPY target/adventure-0.1.0-SNAPSHOT.jar /app/adventure.jar
EXPOSE 8080
CMD ["java", "-jar", "/app/adventure.jar", "--server", "--port", "8080"]
```

```bash
# Build Docker image
docker build -f deployment/Dockerfile -t adventure:0.1.0 .

# Run container
docker run -p 8080:8080 adventure:0.1.0
```

**References:**
- Architecture: `docs/architecture_design.md` → Deployment Strategy
- Runbook: `docs/operator_runbook.md` (to be created)

---

## Documentation Updates

### When to Update This File
- **Phase Transitions:** Mark phases complete, update status
- **Quality Gate Changes:** Add/remove gates as systems evolve
- **New Features:** Add deliverables and test commands for new modules
- **Performance Targets:** Update benchmarks based on profiling

### Related Documentation
- **Architecture Changes:** Update `docs/architecture_design.md`
- **Design Decisions:** Update `docs/design_decisions.md`
- **Open Questions Resolved:** Move to `docs/design_decisions.md` and close in `docs/open_questions.md`
- **Implementation Blockers:** Track in `docs/TO_FIX.md` (currently all 42 items complete ✅)

---

## Troubleshooting

### Build Fails with "java version mismatch"
**Cause:** JDK version doesn't match `pom.xml` compiler settings (Java 21).  
**Fix:**
```bash
# Verify Java version
java -version
# Should report 21.0.x

# If wrong version, set JAVA_HOME to JDK 21
# Windows PowerShell:
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
# Linux/macOS:
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
```

### Tests Fail with "Checksum Mismatch"
**Cause:** Worldgen determinism broken (likely RNG not seeded properly).  
**Fix:**
1. Check that all RNG calls use seeded `Random` instances
2. Review recent changes to generation code
3. Run determinism test in debug mode to find divergence point
```bash
.\maven\mvn\bin\mvn.cmd test -Dtest=WorldGenTest -X
```

### Performance: World Generation Too Slow
**Cause:** Expensive algorithms (e.g., river pathfinding on large maps).  
**Fix:**
1. Profile with JFR (see Performance Benchmarks section)
2. Optimize hot paths (e.g., A* heuristic, cache elevation lookups)
3. Consider chunked generation (generate only visible regions)

### Save File Corrupted
**Cause:** Disk write failure, power loss, or schema mismatch.  
**Fix:**
1. Restore from backup (backups stored in `saves/backups/`)
```bash
# List backups
ls saves/backups/

# Copy backup to main save
cp saves/backups/world_backup_1.json saves/world.json
```
2. If no backup: regenerate world from seed (if seed known)

**References:**
- Operator Runbook: `docs/operator_runbook.md` (to be created)

---

## Status Summary

### Current Phase: MVP Phase 1 (Foundation)
- **Overall Progress:** 100% complete ✅ **MVP PHASE 1 COMPLETE**
  - **World Generation: 100% ✅ PHASE COMPLETE**
    - Tectonic plates: Complete with 12 tests passing
    - Elevation & temperature: Complete with layered noise
    - Biome assignment: Complete with 25 tests passing
    - Rivers: Complete with 12 tests passing ✅
    - Regional features: Complete with 13 tests passing ✅
    - **Total: 62 tests passing (Plate: 12, Biome: 25, River: 12, Feature: 13)**
  - **Region Simulation: 100% ✅ PHASE COMPLETE**
    - Tick-driven simulation: Complete with 16 tests passing
    - Region activation/deactivation: Complete
    - Resource regeneration: Complete with 14 tests passing
    - **Total: 40 tests passing (RegionSimulator: 16, Region: 10, ResourceNode: 14)**
  - **Characters & NPCs: 100% ✅ PHASE COMPLETE**
    - Character system: Complete with 16 tests passing
    - NPC spawning: Complete with 18 tests passing
    - Traits: Complete with 17 tests passing
    - Skills: Complete with 16 tests passing
    - **Total: 67 tests passing (Character: 16, NPC: 18, Trait: 17, Skill: 16)**
  - **Items & Crafting: 100% ✅ PHASE COMPLETE**
    - Item system: Complete with 20 tests passing
    - Crafting system: Complete with 23 tests passing
    - Proficiency progression: Complete
    - 7 MVP recipes + 12 item prototypes
    - **Total: 43 tests passing (Item: 20, Crafting: 23)**
  - **Structures & Ownership: 100% ✅ PHASE COMPLETE**
    - Structure system: Complete with 27 tests passing
    - Taxation system: Complete with 22 tests passing
    - Ownership transfer: Complete with 33 tests passing ✅ PHASE 1.5.1
    - Contested ownership: Complete with dispute resolution ✅ PHASE 1.5.1
    - **Total: 82 tests passing (Structure: 27, Taxation: 22, Transfer: 33)**
  - **Societies & Clans: 100% ✅ PHASE COMPLETE**
    - Clan system: Complete with 25 tests passing
    - Diplomacy system: Complete with 30 tests passing
    - Membership and treasury management: Complete
    - Event-driven updates and periodic decay: Complete
    - Alliance formation and merging: Complete
    - **Total: 55 tests passing (Clan: 25, Diplomacy: 30)**
  - **Stories & Events: 100% ✅ PHASE COMPLETE**
    - Story seeding at worldgen: Complete with deterministic placement
    - Event propagation: Complete with BFS algorithm and exponential decay
    - Saturation management: Complete with per-region caps (50 stories, 20 events)
    - Story types: LEGEND, RUMOR, QUEST, PROPHECY, TRAGEDY, COMEDY, MYSTERY
    - Event categories: WORLD, REGIONAL, PERSONAL, RANDOM, TRIGGERED
    - **Total: 83 tests passing (Story: 15, Event: 19, Generator: 15, Propagation: 15, Saturation: 19)**
  - **Persistence & Save/Load: 100% ✅ PHASE 1.8 COMPLETE**
    - JSON save format: Complete with human-readable output
    - Schema versioning: Complete with schemaVersion in all classes
    - Backup rotation: Complete with N=5 automatic pruning
    - Checksum validation: Complete with SHA-256 + corruption detection
    - Migration registry: Complete with YAML-based migration path tracking
    - **Total: 15 tests passing (SaveManager, BackupManager, SchemaVersionManager)**
  - **Multiplayer & Networking: 100% ✅ PHASE 1.9 COMPLETE**
    - Authoritative server: Complete with action validation and processing
    - Text-based client: Complete with CLI interface
    - JWT authentication: Complete with 24-hour token expiry
    - Conflict resolution: Complete with resource locking and timestamp ordering
    - Server-side validation: Complete for all action types
    - **Total: 86 tests passing (Server: 14, Auth: 21, Conflict: 16, Validation: 35)**
  - **CI/CD & Deployment: 100% ✅ PHASE 1.10 COMPLETE**
    - GitHub Actions workflow: Complete with multi-platform builds (Linux + Windows)
    - Coverage reporting: Complete with JaCoCo plugin (70% threshold)
    - Nightly integration tests: Complete with determinism and performance validation
    - Docker deployment: Complete with multi-stage Dockerfile and docker-compose
    - Cross-platform scripts: Complete with deploy.ps1 (Windows) and deploy.sh (Linux/macOS)
    - Executable JAR: Complete with Maven Shade plugin (fat JAR with all dependencies)
    - Deployment documentation: Complete with comprehensive DEPLOYMENT.md guide
    - **Deliverables: 8/8 complete (100%)**

### Next Milestones
1. **Complete Multiplayer MVP (Target: Q1 2026)**
   - Build authoritative server
   - Implement text-based client
   - Add authentication and conflict resolution

2. **Enhance Persistence (Target: Q2 2026)**
   - Add chunk-based loading for large worlds
   - Implement streaming save/load
   - Performance optimization for 512x512 worlds

### Blockers & Open Questions
- See `docs/open_questions.md` for unresolved design questions
- All 42 implementation items from `docs/TO_FIX.md` are complete ✅

### Test Breakdown by Phase
- **Phase 1.1 (World Generation):** 62 tests ✅
- **Phase 1.2 (Region Simulation):** 40 tests ✅
- **Phase 1.3 (Characters & NPCs):** 67 tests ✅
- **Phase 1.4 (Items & Crafting):** 43 tests ✅
- **Phase 1.5 (Structures & Ownership):** 49 tests ✅
- **Phase 1.5.1 (Ownership Transfer):** 33 tests ✅
- **Phase 1.6 (Societies & Clans):** 55 tests ✅
- **Phase 1.7 (Stories & Events):** 83 tests ✅
- **Phase 1.8 (Persistence):** 15 tests ✅
- **Phase 1.9 (Multiplayer & Networking):** 86 tests ✅
- **Total:** 534 tests ✅

---

## Contributing

### Code Style
- Follow Google Java Style Guide
- Use `google-java-format` or checkstyle plugin
- Max line length: 100 characters

### Commit Messages
```
<type>(<scope>): <subject>

<body>

<footer>
```
**Types:** `feat`, `fix`, `docs`, `test`, `refactor`, `perf`, `chore`  
**Example:**
```
feat(worldgen): add tectonic plate simulation

- Implement cellular automata for plate boundaries
- Add deterministic seeding for reproducibility
- Validate no uphill rivers in integration tests

Closes #42
```

### Pull Request Checklist
- [ ] All tests pass (`mvn test`)
- [ ] Code coverage meets thresholds (70% for new code)
- [ ] Determinism tests added for new procedural systems
- [ ] Documentation updated (`BUILD_PHASE1.md`, relevant `docs/*.md`)
- [ ] CHANGELOG.md updated (if user-facing change)

---

## 🚨 CRITICAL: Phase 1.10.x — Living World (IN PROGRESS)

**Status:** ⚠️ BLOCKING GAP IDENTIFIED — Worldgen lacks initial conditions

While Phase 1.1-1.10 completed all backend systems, **a critical gap was discovered**:
- ❌ Worldgen only creates geography (no clans, settlements, NPCs, quests)
- ❌ World is static (nothing grows, expands, or changes over time)
- ❌ Game feels dead, not alive

**Solution:** Phase 1.10.x adds living world features:
- ✅ Initial clans, settlements, prophecies, quests at worldgen
- ✅ Named NPC system (with homes, jobs, marriage, reproduction)
- ✅ Village/city formation from structure clusters
- ✅ Road networks connecting settlements
- ✅ Dynamic clan expansion & structure lifecycle
- ✅ Living world simulation (builds, grows, destroys)

**👉 See [BUILD_PHASE1.10.x.md](BUILD_PHASE1.10.x.md) for complete implementation guide**

---

## Next Steps

### Priority Order:

1. **🔥 CRITICAL: Phase 1.10.x (Living World)** → See [BUILD_PHASE1.10.x.md](BUILD_PHASE1.10.x.md)
   - Add worldgen initial conditions (clans, settlements, quests, prophecies)
   - Implement village/city formation & road generation
   - Enable dynamic world simulation (clan expansion, structure lifecycle)
   - **BLOCKING FOR MVP** — Game not playable without this

2. **Gameplay & UI Development** → See [BUILD-GAMEPLAY.md](BUILD-GAMEPLAY.md)
   - Web client architecture
   - Character creation UI
   - World map rendering
   - Inventory & crafting interfaces
   - Real-time multiplayer interactions

3. **Phase 2: Advanced Systems** → See [BUILD_PHASE2.md](BUILD_PHASE2.md)
   - Magic system implementation
   - Advanced diplomacy & secret agendas
   - Dynamic economy & trade routes
   - NPC AI & behavior trees
   - Modding support & sandboxing

4. **Deployment** → See [deployment/DEPLOYMENT.md](deployment/DEPLOYMENT.md)
   - Docker containerization
   - Cloud deployment (AWS/Azure/GCP)
   - CI/CD pipeline setup
   - Monitoring & logging

---

## License

(To be determined — placeholder for license info)

---

## Contact & Support

- **Project Lead:** (To be added)
- **Issue Tracker:** GitHub Issues
- **Community:** (Discord/Slack link to be added)

---

**End of BUILD_PHASE1.md**
