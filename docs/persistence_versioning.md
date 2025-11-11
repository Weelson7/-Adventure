# Persistence & Versioning

This document specifies the save/load strategy, schema versioning, and migration guidance.

Recommended formats
- Terrain/base generation: regenerateable from seed + parameters. Store parameters and seed; persist only deltas and features that cannot be regenerated.
- Runtime state (characters, societies, structures, dynamic features): versioned JSON stored per-region (region chunks) or a compact binary (protobuf) for performance-sensitive builds.

Schema versioning
- Each persisted object MUST include a `schemaVersion` integer and `type` string.
- Maintain a migration registry: a sequence of migration steps that transform older schema versions to the current version.

Atomicity & backups
- Writes must be atomic: write to a temp file then rename/replace original.
- Keep last N backups (configurable) to recover from partial corruption.

Deterministic fields to store
- Global seed and generation parameters.
- lastProcessedTick for regions and regions' delta queues.

Migration guidance
- Provide one-way migration scripts (v1 -> v2 -> v3). Prefer backward-compatible reads where feasible.
- For large worlds, support streaming migrations by region chunks so migration can be incremental.

Corruption handling
- Validate checksums on load. On checksum failure, attempt to load backup; if unavailable, attempt regeneration from seed for terrain and fail gracefully for dynamic state (report missing state to operators).

### Persistence Failure Recovery Procedures (exact steps)

**Checksum Validation Failure:**
1. Detect checksum mismatch on chunk/file load.
2. Log error with file path, expected vs actual checksum, timestamp.
3. Attempt to load most recent backup (last N backups retained, default N=5).
4. If backup valid, restore from backup and flag original file as corrupted for operator review.
5. If all backups corrupted, attempt partial recovery:
   - For terrain chunks: regenerate from seed + parameters (deterministic).
   - For dynamic state (characters, societies): report data loss to operator and provide rollback options (manual restore from earlier save).

**Partial Save Corruption:**
- If only some chunks corrupted, load valid chunks and mark corrupted regions as inactive.
- Operators can trigger region rebuild (regenerate terrain, respawn default NPCs, clear dynamic state).
- Player characters in corrupted regions are teleported to nearest valid region or spawn point.

**Schema Migration Failure:**
- If migration script fails mid-stream, halt migration and rollback to pre-migration backup.
- Log migration error details (object ID, schemaVersion, error message).
- Operator must fix migration script or data issue before retrying.

**Full Save Corruption (catastrophic):**
- If primary save and all backups corrupted, offer operator two choices:
  1. Regenerate world from seed (loses all dynamic state but preserves world structure).
  2. Attempt forensic recovery using manual tools (parse raw files, extract salvageable data).
- Document loss in incident log and notify players if multiplayer.

**Backup Rotation Policy:**
- Keep last 5 save snapshots (configurable). Rotate on each successful full save.
- Snapshots include timestamp and schemaVersion metadata for easy rollback selection.

**Automated Health Checks:**
- Run periodic checksum validation on idle servers (e.g., nightly).
- Alert operators if checksums fail or backup count drops below minimum.

Notes:
- Document recovery playbooks in operator runbooks (separate from design docs).
- Test recovery procedures in staging environments before production deployment.


Performance notes
- Use chunked storage for the WorldGrid (e.g., 64x64 tile chunks). Load-on-demand for active regions and evict least-recently-used chunks.
