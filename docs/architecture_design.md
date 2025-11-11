# !Adventure Game: Architecture Design

## Overview
This document outlines the high-level architecture for the !Adventure game, focusing on modularity, scalability, and performance. The design ensures that only player-involved areas are deeply simulated, while distant regions and entities evolve in a simplified manner.

---

## Core Principles
- **Modular Systems:** Each core feature (world, societies, structures, objects, characters, stories, etc.) is implemented as a separate module/service.
- **Region-Based Simulation:** The world is divided into regions. Regions with active players are deeply simulated; distant regions use simplified logic.
- **Event-Driven Updates:** Player actions trigger detailed updates in local regions. Distant regions, structures, and societies update periodically or when interacted with.
- **Data Persistence:** All game state is saved and loaded efficiently, with lazy loading for distant regions.
- **Extensibility:** New features and modules can be added without major refactoring.

---

## Time Model & Ticks

To avoid ambiguity, the architecture uses a tick-driven time model across simulations and persistence.

- Default tick length: 1 second (configurable globally). Ticks are the atomic unit for simulation updates and persistence snapshots.
- Active region tick rate: activeTickRateMultiplier = 1 (full speed).
- Background region tick rate: backgroundTickRateMultiplier = 1/60 (one background update per 60 real ticks by default).
- lastProcessedTick: each region and long-running simulation actor must store `lastProcessedTick` to support resynchronization.
- Resynchronization: when a region becomes active, the engine applies queued deltas or replays summarized events deterministically from `lastProcessedTick` to current tick.

Notes: These values are defaults and should be configurable per deployment and world preset. Use design decisions doc (`docs/design_decisions.md`) as the canonical source.

## Main Components

### 1. World Manager
- Handles world grid, region loading/unloading, biome assignment, and regional features.
- Deep simulation for active regions; simplified evolution for distant regions.

### 2. Region Simulator
- Manages local simulation (NPCs, structures, events) for regions with players.
- Simplified tick-based evolution for inactive regions.

### 3. Society Engine
- Tracks clans, kingdoms, relationships, diplomacy, and crises.
- Full simulation for local societies; periodic updates for distant ones.

### 4. Structure System
- Manages buildings, rooms, upgrades, damage, and ownership.
- Deep simulation for structures in active regions; simplified for distant ruins/dungeons.

### 5. Object & Crafting System
- Handles item generation, crafting, magic, durability, and legacy effects.
- Only objects in player regions are fully simulated; distant items evolve by rules.

### 6. Character Manager
- Manages stats, traits, skills, and bestiary.
- Full simulation for player/NPCs in active regions; simplified for distant populations.

### 7. Story & Event Engine
- Generates and propagates stories/events.
- Local impact for player regions; distant stories/events update periodically.

### 8. Game State & Persistence
- Efficient save/load system, lazy loading for distant regions/entities.
- Modular data storage for each system.

---

## Performance Strategies
- **Active Region Focus:** Only simulate in detail where players are present.
- **Simplified Evolution:** Use rule-based updates for distant regions, societies, and structures.
- **Lazy Loading:** Load data for regions/entities only when needed.
- **Event Batching:** Batch updates for distant areas to reduce computation.
- **Modular Services:** Each system can be optimized independently.

---

## Open Questions

## Design Decisions

For canonical design decisions and cross-cutting architectural choices, see **`docs/design_decisions.md`**. Key architectural decisions:

- Tick-driven simulation with configurable tick rate (default 1s).
- Active regions use full simulation; background regions use simplified updates (1/60 tick rate).
- Authoritative server model for multiplayer; deterministic conflict resolution.
- Chunked persistence with schemaVersion and atomic writes.

Refer to `docs/design_decisions.md` for the full authoritative list.

---


## Next Steps
- Define interfaces and data flow between modules.
- Design region loading/unloading logic.
- Specify simplified evolution rules for distant entities.
- Prototype world manager and region simulator.

---

## Altitude & Grid Systems
- The world grid is a 3D system: latitude, longitude, altitude (vertical layers)
- Altitude affects biome, resource, and hazard distribution
- Regional subgrids allow for fine-grained simulation in player-active areas

## Technical Details & Implementation Notes

### World Manager
- Interface: loadRegion(), unloadRegion(), assignBiome(), placeFeature()
- Data: 3D grid structure, region metadata, biome/resource maps
- Handles region activation/deactivation based on player proximity

### Region Simulator
- Interface: simulateRegionTick(), updateNPCs(), processEvents()
- Data: NPC/structure/event lists per region
- Deep simulation for active regions, simplified tick for distant ones

### Society Engine
- Interface: updateClans(), processDiplomacy(), triggerCrisis()
- Data: clan/kingdom objects, relationship graphs, reputation/influence metrics
- Event-driven updates, special handling for cross-region events

### Structure System
- Interface: buildStructure(), upgradeStructure(), damageStructure(), transferOwnership()
- Data: structure/room objects, ownership records, upgrade paths
- Legacy effects and gameplay functions (residency, training, etc.)

### Object & Crafting System
- Interface: generateItem(), craftItem(), castSpell(), restoreItem()
- Data: item objects, crafting recipes, rune/spell definitions, durability stats
- Magic system: rune-based spell creation, mana/stability mechanics

### Character Manager
- Interface: createCharacter(), updateStats(), learnSkill(), inheritTrait()
- Data: character objects, stat/trait/skill dictionaries, bestiary
- Handles stat progression, trait inheritance, skill retraining

### Story & Event Engine
- Interface: generateStory(), propagateEvent(), manageSaturation()
- Data: story/event objects, propagation maps, event probability controls
- Triggers new stories/events from player actions, manages local/global impact

### Game State & Persistence
- Interface: saveGame(), loadGame(), lazyLoadRegion(), updateDataStore()
- Data: modular save files, region/entity caches

### Persistence strategy (summary)

- Terrain & deterministic data: regenerateable from seed + parameters. Persist only seed and parameter set plus non-regenerable deltas (features, placed structures).
- Dynamic runtime state (characters, societies, structures, item ownership): persisted per-region chunk with `schemaVersion`, `type`, `lastProcessedTick`.
- Use chunked region files (e.g., 64x64 tiles) with checksums and backup retention. For very large worlds, support streaming, region-by-region migration.
- On writes: use atomic write (temp file + rename) and rotate backups. Validate checksums on load and attempt recovery via backups or regeneration where possible.

**Persistence Strategy for Non-World Modules (detailed):**

- **Characters**: stored per-region (active region files) or in a centralized character database for offline/cross-region characters. Include `schemaVersion`, `lastOnlineTick`, and full stat/trait/inventory snapshots. Use lazy loading: load character data only when region becomes active or player logs in.
- **Societies/Clans/Kingdoms**: stored in a separate societies database file (or region-associated if localized). Include `RelationshipRecord` maps, treasury, and event queues. Update persistence on state changes (e.g., treasury transactions, membership changes).
- **Items**: items in active regions stored with region chunk; items in player inventories stored with character record. Use `historyReferenceId` to link to stories (lazy-load story details). For legendary items, always store full evolution state.
- **Region-chunk storage**: each region chunk (default 64x64 tiles) is a single persistence unit with metadata: `id`, `lastModifiedTick`, `checksum`, `compressedData`. Load/unload chunks as regions activate/deactivate. Chunk files named by coordinate (e.g., `region_x0_y0.chunk`).

**Migration Workflow (summary, see `docs/persistence_versioning.md` for full details):**

1. On load, check `schemaVersion` of each persisted object against current expected version.
2. If mismatch, look up migration path in migration registry (e.g., v1→v2→v3 chain).
3. Apply migration transforms sequentially. For region-chunked data, migrate one chunk at a time (streaming migration) to avoid loading entire world into memory.
4. Write migrated data with updated `schemaVersion` and new checksum. Keep old backup until migration confirmed successful.
5. Operator tools exist to dry-run migrations, validate checksums, and rollback to previous backup if migration fails.

**Key Principles:**
- Minimize in-memory footprint: load only active regions and their dependencies.
- Atomic updates: all state changes are transactional where possible (e.g., ownership transfer, treasury debit/credit).
- Audit trail: critical state changes (ownership, taxation, society treasury) are logged to append-only audit logs for debugging and rollback support.

See `docs/persistence_versioning.md` for full guidance and migration patterns.
- Ensures efficient data management and recovery

---

## Exhaustive Feature Specifications

### World Manager
- Manages the 3D world grid (latitude, longitude, altitude)
- Assigns biomes and places regional features
- Loads/unloads regions based on player proximity
- Tracks region metadata (active/inactive, resources, hazards)
- Integrates subgrid system for fine-grained simulation
- Coordinates deep/simplified simulation with Region Simulator

### Region Simulator
- Simulates NPCs, structures, and events in active regions
- Applies simplified evolution logic for distant regions
- Processes region-specific events, migrations, and resource changes
- Maintains NPC, structure, and event lists per region
- Communicates region state changes to other modules

### Society Engine
- Tracks clans, kingdoms, relationships, diplomacy, crises
- Calculates loyalty, reputation, influence, alignment, race affinity
- Handles clan/kingdom merging, destruction, secret relationships
- Triggers and resolves crises and diplomatic events
- Maintains relationship graphs and metrics

### Structure System
- Manages buildings, rooms, upgrades, damage, ownership
- Implements legacy effects and gameplay functions (residency, training, learning, fortification)
- Handles ownership transfer, taxation, asset management
- Maintains structure/room objects, ownership records, upgrade paths

### Object & Crafting System
- Generates, crafts, and modifies items (weapons, tools, armor, etc.)
- Implements magic system (rune-based spells, mana, stability)
- Tracks item durability, restoration, evolution
- Assigns legacy/story effects to objects
- Maintains item objects, crafting recipes, rune/spell definitions, durability stats

### Character Manager
- Creates and manages characters (stats, traits, skills, bestiary)
- Handles stat progression, trait inheritance, skill retraining
- Assigns race/species, manages playable/NPC status
- Maintains character objects, stat/trait/skill dictionaries, bestiary

### Story & Event Engine
- Generates and propagates stories/events
- Manages event probability, saturation, local/global impact
- Triggers new stories/events from player actions
- Maintains story/event objects, propagation maps

### Game State & Persistence
- Saves/loads game state, lazy loads regions/entities
- Manages modular data storage and recovery
- Maintains save files, region/entity caches
- Interfaces with all modules for data persistence

---

## Simulation Logic
- Deep simulation for regions with players: full NPC/structure/event updates
- Simplified evolution for distant regions: periodic, batched updates based on rules
- Region activation/deactivation managed by player movement and proximity
- Exception handling for cross-region events/migrations
- Resource/hazard balancing via biome/altitude/grid systems
- Event batching and lazy loading for performance

---

## Multiplayer Architecture & Player Interaction

### Multiplayer Handling
- The world is managed server-side for consistency and security
- Clients send queries (read requests) and put requests (actions/commands) to the server
- Server validates, checks, and processes all client requests
- Only validated actions are applied to the world state
- Server broadcasts relevant updates to affected clients/players
- Server manages region activation/deactivation based on player locations

### Conflict Resolution Strategy for Multiplayer (authoritative server approach)

**Authoritative Server Model:**
- All state-changing operations (ownership transfer, diplomacy actions, resource collection, combat) are processed server-side.
- Clients send action requests; server validates, resolves conflicts, and broadcasts results.

**Transaction Ordering:**
- All operations assigned a monotonic `operationId` and `tick` timestamp on receipt.
- Conflicting operations (e.g., two players trying to claim same structure) are resolved by:
  1. Tick order (earlier tick wins).
  2. If same tick, deterministic tiebreaker: hash(actorId + targetId + seed) determines winner.

**Optimistic Concurrency:**
- Clients can show predicted outcomes immediately (optimistic UI).
- Server sends confirmation or rejection; clients rollback if rejected.

**Locks for Critical Resources:**
- Structures, high-value items, and society treasuries use short-lived locks (default 1 tick) during transactions.
- If lock acquisition fails, operation queued for retry on next tick.

**CRDTs (optional, for future extensibility):**
- For reputation/influence metrics that can tolerate eventual consistency, consider conflict-free replicated data types.
- MVP uses authoritative server for simplicity; CRDTs can be added later for performance optimization.

**Rollback & Dispute Resolution:**
- Ownership transfer events are logged with full context (actorId, targetId, tick, operationId).
- Operators can manually rollback disputed transactions using audit logs and idempotent rollback operations.

Notes:
- Link to `docs/structures_ownership.md` for ownership-specific conflict resolution rules.
- See `docs/design_decisions.md` for canonical transaction ordering and tiebreaker algorithms.


### Player Gameplay Flow
- Players connect to the server and receive their current region/world state
- Players interact via text commands (move, interact, talk, fight, craft, cast spells, etc.)
- Client sends player actions to the server for validation and processing
- Server updates the world, NPCs, structures, and events based on player actions
- Players receive feedback, results, and updated world state from the server
- Multiplayer features include:
  - Cooperative and competitive play
  - Shared world events and crises
  - Real-time or turn-based interactions (configurable)
  - Chat, trading, alliances, and diplomacy
- Server ensures fair play, resolves conflicts, and manages persistence

---

## Networking & Security
- Use TCP for reliable communication, with optional UDP for real-time updates
- Encrypt all client-server traffic (TLS/SSL)
- Authenticate users with secure tokens and password hashing
- Validate all client requests server-side to prevent exploits
- Implement anti-cheat measures and monitor suspicious activity

### Security model (summary)

- Roles: admin, moderator, developer, player. Each role maps to capability sets (read/write/delete/apply-mods).
- Capability-based permissions: granular permissions for file uploads, mod activation, structure management, and treasury actions.
- Rate limiting: per-account and per-IP rate limits for critical actions (e.g., ownership transfer, mod uploads, structure build) with configurable thresholds and cooldowns.
- Audit: write append-only audit logs for security-sensitive actions (ownership transfers, taxation changes, migration runs). Logs include actor, tick, action, and checksum of affected objects.
- Mod sandboxing: prefer data-only mods for MVP; for scripted mods use WASM sandbox with host-call restrictions and resource limits.

See `docs/modding_and_security.md` for details.

## Error Handling & Fault Tolerance

- Error taxonomy: categorize errors as Recoverable (soft), Partial-Failure (requires manual or scripted repair), and Fatal (requires operator intervention). Define per-module recovery steps.
- Automatic backups and periodic save points.
- Transactional updates for critical game state changes; prefer idempotent operations and replayable event logs.
- Graceful recovery from crashes and network failures; attempt auto-repair using backups and regeneration where possible.
- Rollback: provide operator-driven rollback tools with clear recovery playbooks documented in `docs/persistence_versioning.md`.

## Scalability & Load Balancing
- Support for horizontal scaling: multiple servers/shards for large worlds
- Dynamic region allocation and migration between servers
- Load balancer to distribute player connections and region simulations
- Cloud deployment options for elastic scaling

### Scalability Thresholds & Targets (concrete constraints)

Define numeric caps and monitoring hooks to prevent runaway resource usage:

**Max Active Regions:**
- Default max: 100 active regions per server instance (tunable based on hardware).
- Exceeding this triggers load-balancing or region migration to secondary servers.
- Monitoring: track `activeRegionCount` and alert if >80% of cap for sustained period.

**NPC Caps per Region:**
- Soft cap: 500 NPCs per region. Hard cap: 1000 NPCs per region.
- Exceeding soft cap triggers simplified NPC AI (batch updates, reduce pathfinding frequency).
- Exceeding hard cap prevents new NPC spawns and triggers migration/culling events.

**Memory/CPU Targets:**
- Target: <4GB RAM per active region on average; <8GB peak.
- CPU: <50% CPU per active region sustained; burst to 80% acceptable for event processing.
- If thresholds exceeded, flag region for optimization review or downgrade to simplified simulation.

**Persistence Churn:**
- Target: <10 chunk writes per second per region under normal load.
- Batch persistence writes every N ticks (default 10) to reduce I/O churn.

**Monitoring Hooks:**
- Expose metrics: `activeRegionCount`, `npcCountPerRegion`, `memoryUsagePerRegion`, `persistenceWriteRate`, `tickProcessingTime`.
- Operators can set alerts and auto-scaling policies based on these metrics.

Notes:
- These are initial targets; adjust based on profiling and load testing.
- Document actual observed limits in deployment notes and update `docs/design_decisions.md` as needed.


## User Account & Session Management
- Secure account creation and login (email, OAuth, etc.)
- Persistent sessions with reconnect and timeout handling
- Store player progress and preferences server-side
- Session tokens for stateless authentication

## Logging & Analytics
- Centralized logging of player actions, server events, and errors
- Real-time analytics dashboard for monitoring performance and usage
- Automated alerts for anomalies or critical failures
- Tools for debugging, profiling, and replaying sessions

## Modding & Extensibility
- Plugin system for user-generated content and mods
- Scripting API for custom events, items, and mechanics
- Safe sandboxing for third-party code
- Documentation and examples for mod developers

## Client UI/UX
- Text-based interface with command auto-completion and help
- Customizable themes and accessibility options
- Real-time feedback and notifications for player actions
- Support for both desktop and web clients

## Testing & Deployment

- Testing frameworks: choose a language-native test framework (e.g., JUnit for Java) and add deterministic-generation tests. Coverage goals: aim for 70% unit coverage for core modules, increase to 85% for critical modules (persistence, region simulation).
- CI: run unit tests on every PR, run heavier integration/regression suites nightly (map diffs, river/pathfinding tests).
- Load testing: include simulated player loads for region activation/deactivation and persistence churn.


## Documentation & Developer Guidelines
- Comprehensive code and API documentation
- Contribution guidelines and code review standards
- Onboarding materials for new developers
- Regular architecture/design reviews and updates
