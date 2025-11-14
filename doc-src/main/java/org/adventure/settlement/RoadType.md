# RoadType

**Package:** `org.adventure.settlement`  
**Type:** Enum  

---

## Overview

`RoadType` defines the three tiers of roads in the game world. Roads can be progressively upgraded as traffic increases, improving their quality and visual appearance.

---

## Enum Values

### DIRT
- **Description**: Basic dirt road, created by default
- **Upgrade From**: N/A (initial tier)
- **Upgrade To**: STONE (when traffic >= 50)
- **Characteristics**: Lowest quality, auto-generated

### STONE
- **Description**: Stone-paved road, upgraded from DIRT
- **Upgrade From**: DIRT (traffic >= 50)
- **Upgrade To**: PAVED (when traffic >= 80)
- **Characteristics**: Improved quality, medium tier

### PAVED
- **Description**: Fully paved road, highest tier
- **Upgrade From**: STONE (traffic >= 80)
- **Upgrade To**: N/A (final tier)
- **Characteristics**: Best quality, no further upgrades

---

## Upgrade Progression

```
DIRT (traffic >= 50) → STONE (traffic >= 80) → PAVED
```

### Upgrade Rules
1. Roads start as DIRT when auto-generated
2. Traffic level determines upgrade eligibility
3. Upgrades are permanent (no downgrades)
4. PAVED roads cannot be upgraded further

---

## Usage Examples

### Creating a Road with Type
```java
RoadTile road = new RoadTile.Builder()
    .position(10, 20)
    .type(RoadType.DIRT)
    .build();
```

### Checking Road Type
```java
if (road.getType() == RoadType.DIRT) {
    // Handle dirt road logic
}
```

### Type-Based Logic
```java
switch (road.getType()) {
    case DIRT:
        // Basic road behavior
        break;
    case STONE:
        // Improved road behavior
        break;
    case PAVED:
        // Best road behavior
        break;
}
```

---

## Design Decisions

1. **Why three tiers?**: Provides clear progression without overwhelming complexity. Simple upgrade path.

2. **Why named DIRT/STONE/PAVED?**: Clear visual representation that matches common road types in games.

3. **Why no enum methods?**: RoadType is a simple data enum; upgrade logic belongs in RoadTile.

4. **Why no intermediate types?**: Three tiers are sufficient for Phase 1.10.2; more can be added later.

---

## Related Classes

- **RoadTile**: Uses RoadType to represent current road quality
- **RoadGenerator**: Creates roads with default DIRT type
- **VillageManager**: May use road types for settlement classification

---

## Future Enhancements (Post-MVP)

1. **Additional Types**: COBBLESTONE, BRICK, MARBLE for variety
2. **Type Properties**: Speed modifiers, maintenance costs per type
3. **Special Roads**: BRIDGE, TUNNEL as distinct types
4. **Climate-Specific Types**: SAND_ROAD, ICE_ROAD, etc.

---

## References

- Design: `BUILD_PHASE1.10.x.md` → Phase 1.10.2
- Related: `RoadTile.md`, `RoadGenerator.md`
- Tests: `RoadTileTest.md`
