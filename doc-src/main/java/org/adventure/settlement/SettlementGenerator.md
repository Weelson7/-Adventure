# SettlementGenerator

**Package:** `org.adventure.settlement`  
**Type:** Class (Factory)  
**Since:** Phase 1.10.1

---

## Overview

`SettlementGenerator` creates initial settlements at worldgen (1 per clan). Each settlement contains:
- Core structure (GUILD_HALL or TEMPLE)
- 3-5 residential structures (HOUSE)
- 1 commercial structure (SHOP or MARKET)
- Connecting roads

---

## Key Method

### `generateInitialSettlements()`

**Signature:**
```java
public static List<Settlement> generateInitialSettlements(
    long worldSeed,
    List<Clan> clans,
    Biome[][] biomes,
    int worldWidth,
    int worldHeight
)
```

**Algorithm:**
```
1. For each clan:
   - Find suitable center point (flat land, near water)
   - Place core structure (GUILD_HALL or TEMPLE)
   - Radially place 3-5 HOUSE structures (10-15 tiles from center)
   - Place 1 commercial structure (SHOP or MARKET)
   - Generate roads connecting all structures
   - Mark entrance tiles
2. Return settlement list
```

---

## Structure Placement

### Spacing Rules
- Minimum 5 tiles between structure centers
- Maximum 15 tiles from settlement center
- Structures placed in concentric rings

### Entrance Assignment
- Random entrance side (N/E/S/W)
- 1 tile clearance in front of entrance
- Can face toward settlement center or road

---

## Related Classes

- `Settlement` — Entity class
- `SettlementType` — Village/Town/City
- `Structure` — Buildings in settlement
- `WorldGen` — Calls SettlementGenerator in phase 11
