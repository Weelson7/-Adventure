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
