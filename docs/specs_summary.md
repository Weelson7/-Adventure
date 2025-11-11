# Specs Summary & Canonical Defaults

This document collects concrete, implementable defaults and short contracts that unblock development, testing, and operations. These are intended as conservative, reviewable starting points. If a decision is changed, update `docs/design_decisions.md` and note the change here.

## Time & Tick Defaults

- Default tick length: 1 second (configurable globally).
- Active region tick rate multiplier: `activeTickRateMultiplier = 1.0`.
- Background region tick rate multiplier: `backgroundTickRateMultiplier = 1/60` (one background update every 60 real ticks by default).
- Use `lastProcessedTick` per-region and per-chunk to support deterministic resynchronization.

## Background Summarization Contract

When a region is in background (simplified simulation), implementers MUST preserve the following persisted fields at minimum so reactivation resynchronizes deterministically:

- `lastProcessedTick` (int)
- Resource summaries per resource node (currentQuantity, Rmax, regenRate), not full harvest logs
- Structure canonical state (id, ownerId, health, upgrades, permissions, lastUpdatedTick)
- NPC population summaries: counts per role/type, aggregated health/wealth buckets; do NOT require per-NPC transient AI/pathing state
- Event saturation counters and minimal event queue summaries (eventId, status, lastProcessedTick)
- Story counters (active story counts per-type) and top-k story references; full story bodies may be archived/compressed
- Chunk checksums and schemaVersion

Transient runtime-only caches (pathfinding open lists, in-memory heuristics) MUST NOT be persisted.

## Ownership, Taxation & Contested Expiry Defaults

- Default tax rate: `taxRate = 0.05` (5%). Document overrides per world preset in `game_parameters_setup.md`.
- Tax cadence: weekly (every 7 in-game days). Implement as a tax job that runs every N ticks defined by the world preset.
- Grace period for unpaid taxes: `gracePeriod = 14 in-game days` (operator-configurable).
- Seizure threshold (transfer to governing authority): `seizureThreshold = 21 in-game days` after grace period and applicable fines.
- Contested ownership expiry default: `contestedExpiryTicks = 7200` ticks (~2 hours at 1s ticks) — apply as a default for contests created by conflicts; actions that materially change ownership (duel, settlement, operator rollback) should pause the contest timer.

Note: represent in-code as tick counts derived from tick-length and in-game day length; these are defaults to be tuned by ops.

## Event Propagation & Saturation

- Default decay function: exponential with `k = 0.8`.
  - `decay(h) = exp(-k * h)` where `h` is hop count.
- Default baseProbability example: `0.9` (tunable per-event).
- Default maxHops: `6`.
- Per-region caps: `maxActiveStoriesPerRegion = 50`, `maxActiveEventsPerRegion = 20`.
- Saturation effective probability: `effectiveP = baseP * max(0, 1 - (currentCount / maxCap))`.

These values are conservative starting points; experiments may tune them.

## Modding Sandbox Defaults

- MVP: support data-only mods (JSON/YAML) by default.
- For scripted mods (post-MVP), prefer WASM sandboxing with resource limits. Recommended default caps:
  - CPU per invocation: 50 ms
  - Wall time per script run: 200 ms
  - Memory: 16 MB
  - Max host-call count per invocation: 200

Log and auto-disable mods that exceed limits; require sign-off for high-privilege mods.

## Persistence Format & Migration

- Recommendation for initial builds: use JSON (human-readable, easy to migrate). For performance-sensitive builds, provide protobuf/compact binary variants and a migration path.
- Persisted object MUST include: `type` (format: `module/ObjectName`), `schemaVersion` (int), and `lastProcessedTick` where applicable.
- Migration registry format (suggested): a YAML list mapping `{fromVersion, toVersion, module, scriptPath, isBreaking}`. Example:

```yaml
- module: world/Chunk
  from: 1
  to: 2
  script: migrations/world/chunk_v1_to_v2.py
  isBreaking: false
```

- Keep N backups (default 5) and provide streaming region-by-region migration tools.

## Testing & CI Defaults

- Project language inference: repository includes `App.java` at root; recommended initial test stack: Java + JUnit 5 + Mockito + TestContainers for integration tests.
- Deterministic-seed test pattern: supply a seed parameter, run generation twice, compare checksums for plate map, elevation map, and river graph.
- Coverage goals: 70% unit coverage for core modules, 85%+ for persistence and conflict-resolution logic.

## Crafting Progression & Magic Defaults

- Crafting tiers and XP: use the tiers documented in `docs/objects_crafting_legacy.md`. Defaults: Novice (0-99xp), Apprentice (100-299), Journeyman (300-599), Expert (600-999), Master (1000+).
- Mana formula (canonical): `maxMana = baseMana + (castingStat * manaPerStat)`; defaults: `baseMana = 10`, `manaPerStat = 2`.
- Backlash probability: clamp denominator to at least 1 to avoid divide-by-zero; ensure `castingStat >= 1` at runtime.

## Operator Runbook (pointer)

- Create `docs/operator_runbook.md` for step-by-step recovery commands. Minimal checklist:
  1. Validate checksums on startup and run automated backups.
  2. If checksum mismatch: attempt backup restore; if none, regenerate terrain from seed where possible.
  3. For migration failures: rollback to pre-migration backup and run dry-run tools.

(If you want I can add a full `operator_runbook.md` with sample commands and dry-run procedures.)

## Next steps

- Move these canonical defaults into `docs/design_decisions.md` once approved.
- Update `docs/game_parameters_setup.md` to include user-facing presets that reflect these defaults.
- Add an `operator_runbook.md` and optionally a sample CI job and deterministic-seed test scaffold.

---

This file is a pragmatic snapshot to unblock implementation. If you want different default numbers or tighter/looser policies, tell me which ones to change and I will apply a follow-up patch.
