# Design Decisions (centralized)

This file collects the authoritative design decisions that must remain stable and referenced by other docs. Keep entries short; link to detailed documents when needed.

Core decisions (initial):

- Determinism: All procedural generation must accept a seed and a seeded RNG instance. Generation must be reproducible from the seed + parameter set.
- Simulation model: Tick-driven simulation. Default tick = 1s; activeTickRateMultiplier = 1, backgroundTickRateMultiplier = 1/60.
- Active region policy: Regions inside player proximity radius run full simulation every tick; distant regions run simplified updates every N ticks. When a region becomes active, it runs a deterministic resynchronization using stored deltas or replayed event summaries.
- Plate generation: Use Voronoi partitioning for initial plate assignment and per-plate movement vectors for drift. Handle uplift/subduction with iterative rules (no full physics engine).
- Rivers: Use a deterministic priority-queue (Dijkstra-like) downhill flow algorithm with seeded micro-noise to break plateaus.
- Ownership: Ownership is singular (not fractional). Access permissions (owner, clan, kingdom, public) exist separately.
- Biomes: By default, biomes do not directly change character stats. Exceptions (magic zones, hazards) are explicit features with their own rules.
- Persistence: All stateful models include schemaVersion and lastProcessedTick where applicable. Use atomic writes with backups; support migration tools.
- Modding: Export generation hooks and data in JSON/YAML; require sandboxing for code-based mods.

Process:
- When a design decision is changed, increment the decision version and note affected docs/files.

## Canonical Defaults (practical values)

The following values are pragmatic defaults (also summarized in `docs/specs_summary.md`) intended to be the authoritative starting point for implementations and ops. If you change one of these, increment a decision version and update this file.

- Tick-driven defaults:
	- Default tick length: 1s
	- activeTickRateMultiplier = 1.0
	- backgroundTickRateMultiplier = 1/60

- Persistence & migration:
	- Persisted objects MUST include `type` (module/ObjectName), `schemaVersion` (int), and `lastProcessedTick` where applicable.
	- Default backup retention: keep last 5 snapshots.

- Ownership & economy defaults:
	- taxRate = 0.05 (5%)
	- tax cadence: weekly (every 7 in-game days)
	- gracePeriod = 14 in-game days
	- seizureThreshold = 21 in-game days after grace period
	- contestedExpiryTicks = 7200 (approx. 2 hours at 1s ticks)

- Event propagation:
	- Default decay: exponential with k = 0.8
	- default maxHops = 6
	- maxActiveStoriesPerRegion = 50
	- maxActiveEventsPerRegion = 20

- Mod sandboxing (post-MVP):
	- CPU per invocation: 50 ms
	- Wall time per script run: 200 ms
	- Memory: 16 MB

- Testing defaults:
	- Recommended initial stack: Java + JUnit 5 + Mockito (repository contains `App.java`)
	- Deterministic-seed tests for generation: compare checksums for plate map, elevation, and river graph

These are starting defaults to be tuned. Move major changes through the design decision process and mark the change here.
