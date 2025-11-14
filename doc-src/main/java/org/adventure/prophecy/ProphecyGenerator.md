# ProphecyGenerator

**Package:** `org.adventure.prophecy`  
**Type:** Class (Factory)  
**Since:** Phase 1.10.1

---

## Overview

`ProphecyGenerator` creates deterministic prophecies at worldgen. It analyzes world features and biomes to generate 1-3 major prophecies that add narrative depth and long-term goals.

---

## Key Method

### `generateProphecies()`

**Signature:**
```java
public static List<Prophecy> generateProphecies(
    long worldSeed,
    List<RegionalFeature> features,
    Biome[][] biomes
)
```

**Algorithm:**
```
1. Initialize seeded RNG
2. Count major features (VOLCANO, MAGIC_ZONE, ANCIENT_RUINS, etc.)
3. Calculate prophecy count:
   - 1-3 based on world size and feature count
   - Formula: min(3, 1 + (majorFeatures / 5))
4. For each prophecy slot:
   - Select random feature
   - Select prophecy type based on feature
   - Generate trigger conditions
   - Set reveal/trigger times
   - Create Prophecy instance
5. Return list of prophecies
```

**Example:**
```java
Random rng = new Random(worldSeed ^ 0xPROPH);
List<Prophecy> prophecies = ProphecyGenerator.generateProphecies(
    worldSeed, features, biomes);

// Result: 1-3 prophecies
// - Linked to major world features
// - Timed reveal/trigger ticks
// - Player involvement flags
```

---

## Prophecy Generation Rules

### Feature → Prophecy Type Mapping

| Feature Type | Likely Prophecy Type | Example |
|--------------|----------------------|---------|
| VOLCANO | DOOM | "Volcanic eruption destroys clans" |
| MAGIC_ZONE | TRANSFORMATION | "Magic spreads and transforms land" |
| ANCIENT_RUINS | AWAKENING | "Lost civilization returns" |
| SUBMERGED_CITY | AWAKENING | "Sunken city rises from ocean" |
| CAVE_SYSTEM | DOOM | "Dark creatures emerge from caves" |
| SACRED_GROVE | SALVATION | "Sacred tree heals the land" |

### Prophecy Count Formula
```java
int majorFeatures = countMajorFeatures(features);
int prophecyCount = Math.min(3, 1 + (majorFeatures / 5));

// Examples:
// 0-4 features → 1 prophecy
// 5-9 features → 2 prophecies
// 10+ features → 3 prophecies
```

---

## Timing Rules

### Reveal Tick
```java
// When prophecy becomes known to the world
long revealTick = baseDelay + (rng.nextInt(50000));

// baseDelay by type:
// - DOOM: 10,000 ticks (1 year warning)
// - SALVATION: 5,000 ticks (immediate hope)
// - TRANSFORMATION: 0 ticks (known from start)
// - AWAKENING: 20,000 ticks (2 year mystery)
```

### Trigger Tick
```java
// When prophecy activates
long triggerTick = revealTick + fulfillmentDelay;

// fulfillmentDelay by type:
// - DOOM: 50,000 ticks (5 years to prepare)
// - SALVATION: 500,000 ticks (50 years to fulfill)
// - TRANSFORMATION: 100,000 ticks (10 years gradual)
// - AWAKENING: 200,000 ticks (20 years until awakening)
```

---

## Related Classes

- `Prophecy` — Entity class
- `ProphecyType` — Enum for prophecy categories
- `ProphecyStatus` — Enum for prophecy state
- `RegionalFeature` — World features
- `WorldGen` — Calls ProphecyGenerator in phase 13
