# ClanGenerator

**Package:** `org.adventure.world`  
**Type:** Class (Factory)  
**Since:** Phase 1.10.1

---

## Overview

`ClanGenerator` is responsible for deterministic generation of initial clans at worldgen. It determines:
- Number of starting clans (scale with world size)
- Clan spawn locations (biome-aware placement)
- Clan types (nomadic, settled, mixed)
- Starting treasury and member counts
- Integration point for NPC generation (calls `NPCGenerator` after settlements exist)

All generation uses a seeded RNG for reproducibility.

---

## Key Methods

### `generateInitialClans()`

**Signature:**
```java
public static List<Clan> generateInitialClans(
    long worldSeed,
    int worldWidth,
    int worldHeight,
    Biome[][] biomes
)
```

**Algorithm:**
```
1. Compute target clan count: max(3, min(50, (width*height) / 20000))
2. Initialize seeded RNG (e.g., new Random(worldSeed ^ 0xCLAN))
3. Select candidate spawn locations avoiding water and steep terrain
4. Score locations by biome suitability (grassland/forest preferred)
5. Assign clan types based on nearby biome
6. Generate starting treasury and member counts
7. Initialize clan relationships to neutral
8. Return list of Clan instances
```

### `generateNPCsForClans()`

**Signature:**
```java
public static List<NamedNPC> generateNPCsForClans(
    List<Clan> clans,
    Map<String, List<Structure>> clanStructures,
    long worldSeed,
    long currentTick
)
```

**Algorithm:**
```
1. Create seeded RNG using worldSeed ^ 0xNPC
2. For each clan:
   - Get structures for that clan
   - Call NPCGenerator.generateInitialClanPopulation(clan, structures, currentTick, rng)
   - Collect and return all NPCs
```

This method ensures NPC generation is deterministic and uses the same seed-derived sequence across worlds.

---

## Parameters & Tuning

- **Scaling formula:** `max(3, min(50, (width * height) / 20000))`
- **Starting member counts:** One large clan (20-30), others small (5-15)
- **Treasury:** 50-200 gold depending on clan type

---

## Integration

### With `WorldGen`
```java
// In WorldGen.generate(seed)
this.clans = ClanGenerator.generateInitialClans(seed, width, height, biomes);

// After settlements are generated:
this.npcs = ClanGenerator.generateNPCsForClans(clans, clanStructures, seed, 0L);
```

### With `SettlementGenerator`
- `ClanGenerator` assigns clanId to settlements and uses settlement locations for spawning NPCs and assigning homes.

---

## Determinism Guarantees

- Uses seed-derived RNGs (e.g., `new Random(worldSeed ^ 0xCLAN)`) for placement and attributes
- IDs are deterministic (hash-based) to ensure consistent ordering across runs

---

## Testing

**Determinism tests:**
- Same seed → same clan count and IDs
- Member counts stable across runs

**Edge cases:**
- Tiny worlds: minimum 3 clans
- All-water worlds: fallback placement on nearest land tiles

---

## Related Classes

- `Clan` — Data model for clan
- `NPCGenerator` — Populates clans with NamedNPCs
- `SettlementGenerator` — Creates homes before NPCs are assigned
- `WorldGen` — Calls `ClanGenerator` during phase 10
