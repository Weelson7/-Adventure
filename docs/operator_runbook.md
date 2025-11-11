# Operator Runbook — Recovery & Migration

This runbook contains step-by-step procedures for common operator tasks: checksum validation, backup restore, region-chunk recovery, streaming migration, and emergency rollback. Examples use PowerShell (Windows) syntax. Adjust paths and tool names to your deployment.

Important: always test runbook steps in staging before applying them in production.

## 1) Validate checksums (startup health check)

Purpose: detect corrupted chunk files early.

Example (PowerShell):

```powershell
# Compute checksum for a chunk file and compare to stored checksum
$chunkPath = 'data/chunks/region_x0_y0.chunk'
$expected = Get-Content "$chunkPath.checksum"
$actual = Get-FileHash -Algorithm SHA256 $chunkPath | Select-Object -ExpandProperty Hash
if ($expected -ne $actual) { Write-Error "Checksum mismatch for $chunkPath"; exit 1 } else { Write-Host "Checksum OK" }
```

Action: If mismatch, proceed to restore from backup (next step).

## 2) Restore from backup (single chunk)

Purpose: attempt recovery from available backups (default N=5 retained snapshots).

Example (PowerShell):

```powershell
$chunk = 'data/chunks/region_x0_y0.chunk'
$backupDir = 'backups/chunks/region_x0_y0/'
# List backups sorted by timestamp (newest first)
Get-ChildItem $backupDir -Filter '*.chunk' | Sort-Object LastWriteTime -Descending | Select-Object -First 5
# Restore latest valid backup
$restored = $backupDir + 'region_x0_y0.chunk.2025-11-10'
Copy-Item -Path $restored -Destination $chunk -Force
# Recompute checksum and verify
# (run the validate-checksum snippet above)
```

If backup is invalid or unavailable, attempt regeneration from seed (terrain only) and mark dynamic state for operator review.

## 3) Regenerate terrain from seed (for chunked terrain)

Purpose: regenerate deterministic base terrain for chunks using the world seed and parameters.

Example (PowerShell invoking a tool):

```powershell
# Example assumes a CLI tool `worldgen.exe` that can regenerate a chunk
.
# regen chunk command: worldgen.exe regen-chunk --world world-001 --chunk-x 0 --chunk-y 0 --out data/chunks/region_x0_y0.chunk
.
./worldgen.exe regen-chunk --world 'world-001' --chunk-x 0 --chunk-y 0 --out 'data/chunks/region_x0_y0.chunk' --seed 123456
```

Note: regenerated terrain will not restore dynamic objects (items, NPCs) — those require manual respawn or restore from backup.

## 4) Partial save corruption: load valid chunks and quarantine corrupted regions

Procedure:
1. Identify corrupted chunk files via checksum scan.
2. For each corrupted chunk, attempt backup restore.
3. If restore fails, move corrupted chunk to `quarantine/` and mark associated region as `inactive` in region index.
4. Notify operators and provide a remediation ticket.

## 5) Streaming migration (region-by-region)

Purpose: perform schema migrations one chunk at a time to avoid huge memory usage.

Dry-run procedure:

```powershell
# Example dry-run CLI command
./migrate.exe --world world-001 --chunk 'region_x0_y0' --dry-run | Out-File migrate_region_x0_y0_dryrun.log
```

Migration run:

```powershell
# Run migration on a single chunk
./migrate.exe --world world-001 --chunk 'region_x0_y0' --script migrations/world/chunk_v1_to_v2.py
# Validate migrated chunk checksum and store backup of original
```

If migration fails mid-stream:
- Stop the migration process.
- Restore the last pre-migration backup for the affected chunk(s).
- Investigate logs and fix migration script.
- Re-run dry-run and resume.

## 6) Full save catastrophic recovery options

If primary save and all backups are corrupted:
- Option A: Regenerate world from seed (loses all dynamic state). Document incident and notify stakeholders.
- Option B: Forensic recovery: run specialized salvage tools to extract partial state. This is slow and may require engineering time.

## 7) Operator rollback after erroneous migration

Steps:
1. Halt services and make a final backup of current state.
2. Identify the last known-good backup timestamp.
3. Restore backups for affected chunks/regions.
4. Restart services in maintenance mode and validate checksums.
5. Notify QA to run deterministic-seed tests and sanity checks.

## 8) Disabling problematic mods

If a mod corrupts the world or creates instability:
1. Use the server admin interface or drop-in config to disable the mod (remove from active mods list).
2. Restart the mod subsystem or the server in safe-mode.
3. If world state corrupted by a mod, restore from pre-mod backup.

## 9) Quick triage checklist (summary)

- Step 1: Run checksum validation across chunks.
- Step 2: Attempt backup restore for each corrupted chunk.
- Step 3: Regenerate terrain from seed for terrain-only loss.
- Step 4: Quarantine irrecoverable chunks and mark regions inactive.
- Step 5: Run migration dry-run for suspected schema issues.
- Step 6: If catastrophic, escalate to forensic recovery or full regen.

## 10) Logging & incident reporting

- Always capture: file path, expected vs actual checksum, timestamp, operator id, and the action taken.
- Store incident logs under `incidents/` with a human-friendly summary and the raw logs.

---

This runbook is a minimal operational guide. If you want, I can expand it with direct sample CLI tools from your codebase (if you have worldgen/migrate tool names) and add a PowerShell script automating routine checks.
