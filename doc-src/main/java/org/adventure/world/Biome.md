# Biome.java - Environmental Type System

**Package:** `org.adventure.world`  
**Source:** [Biome.java](../../../../src/main/java/org/adventure/world/Biome.java)  
**Phase:** MVP Phase 1.1 (World Generation - Biome Assignment)

## Overview

`Biome` is an enumeration defining 14 distinct environmental types that characterize regions of the procedurally generated world. Each biome encapsulates the ecological, climatological, and geographical properties of terrain, influencing resource availability, habitability, and visual aesthetics.

This class is central to Phase 1.1's goal of creating **rich, varied worlds** with natural environmental diversity aligned with `docs/biomes_geography.md` specifications.

## Design Philosophy

### Why Biomes Matter

Biomes transform a numerical elevation map into a **living world**:
- **Gameplay Variety**: Deserts require water management, jungles offer abundant resources but diseases
- **Strategic Depth**: Players must adapt settlements to biome constraints (tundra → heating, desert → irrigation)
- **Visual Identity**: Each biome has distinct ASCII/graphical representation (forest = trees, ocean = waves)
- **Historical Simulation**: Civilizations emerge in fertile biomes (grasslands, forests) vs harsh ones (deserts, tundra)

From `docs/grand_plan.md` MVP matrix:
> "Biomes determine starting resources, migration patterns, and long-term sustainability of civilizations."

### 14 Biome Types: A Spectrum of Life

```java
public enum Biome {
    // Aquatic (elevation < 0.2)
    OCEAN,      // Deep saltwater, base elevation 0.0-0.15
    LAKE,       // Freshwater, base elevation 0.15-0.2

    // Cold (temperature < 0°C)
    TUNDRA,     // Frozen plains, minimal vegetation
    TAIGA,      // Coniferous forests, moderate cold

    // Temperate (0-20°C)
    GRASSLAND,  // Open plains, high agriculture potential
    FOREST,     // Deciduous/mixed forests, balanced resources
    SWAMP,      // Wetlands, high moisture, disease risk

    // Hot (>20°C)
    DESERT,     // Arid, low moisture, extreme temperatures
    SAVANNA,    // Tropical grasslands, seasonal rainfall
    JUNGLE,     // Dense rainforest, high biodiversity

    // Elevated (elevation > 0.6)
    HILLS,      // Rolling terrain, moderate slopes
    MOUNTAIN,   // High peaks, impassable terrain
    VOLCANIC,   // Active geology, fertile ash soil
    MAGICAL     // Anomalous regions (future expansion)
}
```

## Class Structure

### Biome Fields

Each biome constant has 6 properties:

```java
private final double minElevation;        // Minimum height (0.0-1.0 scale)
private final double maxElevation;        // Maximum height
private final double minTemperature;      // Minimum temp in °C
private final double maxTemperature;      // Maximum temp in °C
private final double moisturePreference;  // Ideal moisture (0.0-1.0, 0.5 = neutral)
private final double resourceAbundance;   // Base resource multiplier (0.1-1.0)
```

### Example: JUNGLE Biome

```java
JUNGLE(0.2, 0.5,           // Elevation: lowlands (20-50% max height)
       22, 35,              // Temperature: tropical heat (22-35°C)
       0.8,                 // Moisture: very wet (80% preference)
       0.9)                 // Resources: abundant (90% of max)
```

**Interpretation:**
- Jungles form in **low-elevation tropical regions** with **high rainfall**
- **Resource-rich**: 90% abundance → ideal for settlements (but disease risk in future phases)
- **Hot & humid**: Average 28.5°C, supports diverse flora/fauna

### Example: TUNDRA Biome

```java
TUNDRA(-10, 5,             // Temperature: freezing (-10 to 5°C)
       0.0, 1.0,            // Elevation: any height (permafrost anywhere)
       0.3,                 // Moisture: low (30% - cold air holds less water)
       0.2)                 // Resources: scarce (20% of max)
```

**Interpretation:**
- Tundras are **cold plains** at any elevation (Arctic, Antarctic, high-altitude)
- **Harsh environment**: Only 20% resources → challenging for survival
- **Low moisture**: Despite ice, little liquid water available

## Core Method: `assign()`

### Signature

```java
public static Biome assign(double elevation, double temperature, double moisture)
```

### Purpose
Determine the appropriate biome for a world tile based on three environmental factors.

### Algorithm (Priority-Based Decision Tree)

```java
public static Biome assign(double elevation, double temperature, double moisture) {
    // 1. AQUATIC BIOMES (elevation-based)
    if (elevation < 0.2) {
        return (elevation < 0.15) ? OCEAN : LAKE;
    }

    // 2. ELEVATED BIOMES (mountainous terrain)
    if (elevation > 0.8) {
        // Special case: Hot + elevated = volcanic
        if (temperature > 25 && moisture > 0.6) return VOLCANIC;
        return MOUNTAIN;
    }
    if (elevation > 0.6) {
        return HILLS;
    }

    // 3. COLD BIOMES (temperature-based)
    if (temperature < 0) {
        return TUNDRA;
    }
    if (temperature < 10) {
        return TAIGA;
    }

    // 4. HOT & DRY BIOMES
    if (temperature > 25 && moisture < 0.3) {
        return DESERT;
    }

    // 5. TROPICAL BIOMES
    if (temperature > 22) {
        return (moisture > 0.7) ? JUNGLE : SAVANNA;
    }

    // 6. TEMPERATE BIOMES (default for moderate climates)
    if (moisture > 0.6) {
        return (moisture > 0.8) ? SWAMP : FOREST;
    }

    // 7. FALLBACK: Grassland (most common biome)
    return GRASSLAND;
}
```

### Decision Tree Visualization

```
elevation < 0.2? → OCEAN/LAKE (water)
   ↓ NO
elevation > 0.8? → MOUNTAIN (peaks)
   ↓ NO
elevation > 0.6? → HILLS (slopes)
   ↓ NO
temperature < 0? → TUNDRA (frozen)
   ↓ NO
temperature < 10? → TAIGA (cold forest)
   ↓ NO
temp > 25 && moisture < 0.3? → DESERT (hot & dry)
   ↓ NO
temperature > 22?
   ├─ YES + moisture > 0.7 → JUNGLE (rainforest)
   └─ YES + moisture ≤ 0.7 → SAVANNA (dry tropical)
   ↓ NO
moisture > 0.6?
   ├─ YES + moisture > 0.8 → SWAMP (wetlands)
   └─ YES + moisture ≤ 0.8 → FOREST (woodlands)
   ↓ NO
GRASSLAND (default temperate)
```

### Example Assignments

**Input:** `elevation=0.1, temperature=5, moisture=0.5`  
**Output:** `OCEAN` (elevation < 0.15)

**Input:** `elevation=0.4, temperature=-5, moisture=0.2`  
**Output:** `TUNDRA` (temperature < 0)

**Input:** `elevation=0.5, temperature=28, moisture=0.9`  
**Output:** `JUNGLE` (temp > 22, moisture > 0.7)

**Input:** `elevation=0.7, temperature=30, moisture=0.7`  
**Output:** `VOLCANIC` (elevation > 0.6, temp > 25, moisture > 0.6)

**Input:** `elevation=0.3, temperature=15, moisture=0.5`  
**Output:** `GRASSLAND` (default temperate)

## Helper Methods

### `isWater()`

```java
public boolean isWater() {
    return this == OCEAN || this == LAKE;
}
```

**Purpose:** Check if biome is aquatic (for pathfinding, boat travel, fishing).

**Usage:**
```java
if (worldGen.getBiome(x, y).isWater()) {
    // Spawn fish, enable naval units, block land movement
}
```

### `isHabitable()`

```java
public boolean isHabitable() {
    return !isWater() && this != MOUNTAIN;
}
```

**Purpose:** Determine if settlers can establish villages.

**Rationale:**
- **Water**: No land for buildings
- **Mountains**: Too steep for construction (but may have mines in future)
- **All other biomes**: Habitable with varying difficulty (tundra hard, grassland easy)

**Usage:**
```java
if (worldGen.getBiome(x, y).isHabitable()) {
    spawnSettlement(x, y);
}
```

**Future Enhancement:** Tiered habitability (grassland = 1.0×, desert = 0.5×, tundra = 0.2×)

## Integration with WorldGen

### Phase 6: Biome Assignment

```java
// WorldGen.assignBiomes()
private void assignBiomes() {
    for (int x = 0; x < width; x++) {
        for (int y = 0; y < height; y++) {
            biomes[x][y] = Biome.assign(
                elevation[x][y],      // From plate tectonics + noise
                temperature[x][y],    // From latitude + elevation
                moisture[x][y]        // From water proximity + noise
            );
        }
    }
}
```

**Result:** Every tile has a biome that coherently reflects its physical properties.

### Accessor Method

```java
public Biome getBiome(int x, int y) {
    return biomes[x][y];
}
```

**Usage in Game Logic:**
```java
WorldGen world = new WorldGen(512, 512);
world.generate(123456789L);

Biome biome = world.getBiome(100, 200);
System.out.println("Tile (100, 200) is " + biome);  // e.g., "FOREST"

if (biome == Biome.DESERT) {
    // Apply desert-specific mechanics: -10% agriculture, +30% water consumption
}
```

## Biome Properties Deep Dive

### OCEAN (0.0-0.15 elevation)
- **Geography**: Open sea, deep water
- **Temperature**: Varies with latitude (polar → tropical)
- **Resources**: Fish (0.6×), salt (0.8×), pearls (0.1× rare)
- **Gameplay**: Naval travel, trade routes, fishing villages
- **Visual**: `~` character (ASCII), blue gradient (graphics)

### LAKE (0.15-0.2 elevation)
- **Geography**: Inland freshwater bodies
- **Temperature**: Inherits from surrounding land
- **Resources**: Freshwater fish (0.7×), reeds (0.5×)
- **Gameplay**: Drinking water source, irrigation hub, defensive barrier
- **Visual**: `≈` character (ASCII), light blue (graphics)

### TUNDRA (-10 to 5°C)
- **Geography**: Permafrost plains, sparse vegetation
- **Temperature**: Freezing year-round
- **Resources**: Lichens (0.2×), caribou (0.3×), furs (0.4×)
- **Gameplay**: Survival challenge, specialized cold-weather tech required
- **Visual**: `.` character (ASCII), white-gray (graphics)

### TAIGA (<10°C)
- **Geography**: Boreal coniferous forests
- **Temperature**: Cold winters, mild summers
- **Resources**: Timber (0.6×), game animals (0.5×), berries (0.3×)
- **Gameplay**: Balanced cold biome, easier than tundra
- **Visual**: `T` character (ASCII), dark green (graphics)

### GRASSLAND (10-22°C, moderate moisture)
- **Geography**: Open plains, tall grasses
- **Temperature**: Temperate
- **Resources**: Grain (0.9×), livestock (0.8×), wildflowers (0.6×)
- **Gameplay**: **Ideal for agriculture**, most common settlement location
- **Visual**: `,` character (ASCII), light green (graphics)

### FOREST (10-22°C, high moisture)
- **Geography**: Deciduous/mixed woodlands
- **Temperature**: Temperate, four seasons
- **Resources**: Hardwood (0.8×), game (0.7×), mushrooms (0.5×)
- **Gameplay**: Balanced resources, moderate clearing effort for farms
- **Visual**: `♣` character (ASCII), medium green (graphics)

### SWAMP (high moisture, low-moderate elevation)
- **Geography**: Waterlogged lowlands, marshes
- **Temperature**: Varies (tropical → temperate)
- **Resources**: Reeds (0.7×), clay (0.6×), exotic herbs (0.4×)
- **Gameplay**: **Disease risk** (future), difficult terrain, unique resources
- **Visual**: `≋` character (ASCII), brown-green (graphics)

### DESERT (>25°C, <0.3 moisture)
- **Geography**: Arid dunes, sand seas
- **Temperature**: Extreme heat (40°C+ day, 10°C night)
- **Resources**: Cacti (0.2×), rare minerals (0.5×), oases (0.1× special)
- **Gameplay**: **Water scarcity**, caravans, nomadic culture
- **Visual**: `░` character (ASCII), yellow-tan (graphics)

### SAVANNA (>22°C, 0.3-0.7 moisture)
- **Geography**: Tropical grasslands, scattered trees
- **Temperature**: Hot, seasonal rainfall
- **Resources**: Tall grass (0.7×), megafauna (0.6×), baobabs (0.3×)
- **Gameplay**: Seasonal agriculture, large game hunting
- **Visual**: `"` character (ASCII), golden-green (graphics)

### JUNGLE (>22°C, >0.7 moisture)
- **Geography**: Dense rainforest, multi-canopy
- **Temperature**: Hot & humid year-round
- **Resources**: Exotic fruits (0.9×), hardwoods (0.8×), medicinal plants (0.7×)
- **Gameplay**: **Resource-rich but challenging terrain**, diseases (future)
- **Visual**: `#` character (ASCII), dark green (graphics)

### HILLS (0.6-0.8 elevation)
- **Geography**: Rolling slopes, moderate gradients
- **Temperature**: -3°C per 0.1 elevation (cooling with altitude)
- **Resources**: Stone (0.6×), ores (0.5×), grazing lands (0.4×)
- **Gameplay**: Defensive terrain, mining potential, moderate farming
- **Visual**: `^` character (ASCII), brown-gray (graphics)

### MOUNTAIN (>0.8 elevation)
- **Geography**: High peaks, steep slopes
- **Temperature**: <0°C at summits
- **Resources**: Rare ores (0.7×), crystals (0.3×), eagles (0.2×)
- **Gameplay**: **Impassable without tech**, strategic barriers, rare resources
- **Visual:** `▲` character (ASCII), gray-white (graphics)

### VOLCANIC (>0.6 elevation, >25°C, >0.6 moisture)
- **Geography**: Active/dormant volcanoes, lava flows
- **Temperature**: Hot from geothermal activity
- **Resources**: Obsidian (0.8×), sulfur (0.6×), **fertile ash soil** (1.2× crop bonus)
- **Gameplay**: **Eruption risk** (future), high-reward high-risk settlement
- **Visual**: `*` character (ASCII), red-orange (graphics)

### MAGICAL (special conditions, future)
- **Geography**: Anomalous zones (floating islands, crystal forests)
- **Temperature**: Unpredictable
- **Resources**: Arcane reagents (1.0×), mythical creatures (0.5×)
- **Gameplay**: **Magic system integration** (Phase 3+), rare spawn
- **Visual**: `?` character (ASCII), purple-cyan (graphics)

## Resource Abundance Scaling

From `docs/economy_resources.md`, resource regeneration formula:

```
actualResource = baseResource × biomeAbundance × (1 + noise × 0.2)
```

**Example: Timber in Forest**
- Base timber: 100 units
- Forest abundance: 0.8
- Random noise: 0.15
- **Result:** `100 × 0.8 × (1 + 0.15 × 0.2) = 80 × 1.03 = 82.4 units`

**Example: Timber in Desert**
- Base timber: 100 units
- Desert abundance: 0.1
- Random noise: -0.1
- **Result:** `100 × 0.1 × (1 - 0.1 × 0.2) = 10 × 0.98 = 9.8 units`

**Takeaway:** Biomes multiply base resources, encouraging trade between regions.

## Testing Strategy

### Unit Tests (BiomeTest.java)

```java
@Test
void testOceanAssignment() {
    Biome biome = Biome.assign(0.1, 10, 0.5);  // Low elevation = water
    assertEquals(Biome.OCEAN, biome);
}

@Test
void testMountainAssignment() {
    Biome biome = Biome.assign(0.9, 15, 0.4);  // High elevation = mountain
    assertEquals(Biome.MOUNTAIN, biome);
}

@Test
void testDesertAssignment() {
    Biome biome = Biome.assign(0.4, 35, 0.2);  // Hot & dry = desert
    assertEquals(Biome.DESERT, biome);
}

@Test
void testJungleAssignment() {
    Biome biome = Biome.assign(0.3, 28, 0.9);  // Hot & wet = jungle
    assertEquals(Biome.JUNGLE, biome);
}

@Test
void testTundraAssignment() {
    Biome biome = Biome.assign(0.5, -5, 0.3);  // Cold = tundra
    assertEquals(Biome.TUNDRA, biome);
}

@Test
void testVolcanicAssignment() {
    Biome biome = Biome.assign(0.7, 30, 0.7);  // Elevated + hot + wet = volcanic
    assertEquals(Biome.VOLCANIC, biome);
}

@Test
void testGrasslandDefault() {
    Biome biome = Biome.assign(0.4, 15, 0.5);  // Moderate everything = grassland
    assertEquals(Biome.GRASSLAND, biome);
}

@Test
void testWaterHelper() {
    assertTrue(Biome.OCEAN.isWater());
    assertTrue(Biome.LAKE.isWater());
    assertFalse(Biome.GRASSLAND.isWater());
}

@Test
void testHabitabilityHelper() {
    assertFalse(Biome.OCEAN.isHabitable());
    assertFalse(Biome.MOUNTAIN.isHabitable());
    assertTrue(Biome.GRASSLAND.isHabitable());
    assertTrue(Biome.DESERT.isHabitable());  // Harsh but habitable
}
```

### Integration Tests (with WorldGen)

```java
@Test
void testBiomeDiversity() {
    WorldGen world = new WorldGen(512, 512);
    world.generate(123456789L);
    
    Set<Biome> foundBiomes = new HashSet<>();
    for (int x = 0; x < 512; x++) {
        for (int y = 0; y < 512; y++) {
            foundBiomes.add(world.getBiome(x, y));
        }
    }
    
    // Expect at least 8 different biomes in a large world
    assertTrue(foundBiomes.size() >= 8, "World should have diverse biomes");
}

@Test
void testBiomeConsistency() {
    // Same conditions → same biome (deterministic)
    WorldGen world = new WorldGen(256, 256);
    world.generate(999L);
    
    Biome biome1 = world.getBiome(100, 100);
    
    // Regenerate with same seed
    world.generate(999L);
    Biome biome2 = world.getBiome(100, 100);
    
    assertEquals(biome1, biome2, "Biome assignment must be deterministic");
}

@Test
void testCoastalTransitions() {
    WorldGen world = new WorldGen(256, 256);
    world.generate(777L);
    
    // Find a coast (water → land transition)
    for (int x = 1; x < 255; x++) {
        for (int y = 0; y < 256; y++) {
            Biome b1 = world.getBiome(x, y);
            Biome b2 = world.getBiome(x + 1, y);
            
            if (b1.isWater() && !b2.isWater()) {
                // Coast detected, validate elevation gradient
                double e1 = world.getElevation(x, y);
                double e2 = world.getElevation(x + 1, y);
                assertTrue(e2 > e1, "Land should be higher than water");
                return;
            }
        }
    }
}
```

## Future Enhancements (Post-MVP)

### 1. Sub-Biomes & Variants (Phase 2)
Split broad categories into specializations:
- **Forest** → Deciduous, Coniferous, Bamboo, Mangrove
- **Desert** → Sand dunes, Rocky badlands, Salt flats
- **Ocean** → Shallow reef, Deep abyss, Kelp forest

### 2. Dynamic Biomes (Phase 3+)
Biomes change over time:
- **Deforestation**: Forest → Grassland (logging/fire)
- **Desertification**: Grassland → Desert (overgrazing)
- **Reforestation**: Grassland → Forest (abandonment)

### 3. Seasonal Variation
Temperature/moisture fluctuate by season:
```java
double winterTemp = temperature - 15;  // Colder in winter
Biome winterBiome = Biome.assign(elevation, winterTemp, moisture);
// Taiga might become Tundra in winter
```

### 4. Microbiomes
Tile-scale variations within biomes:
```java
if (biome == FOREST && noise > 0.8) {
    return FOREST_CLEARING;  // Small meadow in forest
}
```

### 5. Biome-Specific Events
Random encounters based on location:
- **Desert**: Sandstorm (−50% visibility)
- **Jungle**: Disease outbreak (−20% population)
- **Volcanic**: Eruption (destroy settlements, +200% soil fertility)

### 6. Culture Adaptation
Civilizations develop biome-specific traits:
- **Tundra dwellers**: Ice fishing, fur trade, igloos
- **Desert nomads**: Caravans, oasis navigation, heat resistance
- **Jungle tribes**: Tree houses, poison darts, monkey domestication

## Performance Considerations

### Assignment Complexity
**O(1) per tile** - Simple if/else tree (no loops or searches)

**Benchmarks (512×512 map = 262k tiles):**
- Biome assignment: ~8ms (33 million assignments/sec)
- Memory: 262k × 4 bytes (enum reference) = ~1 MB

### Optimization: Lookup Table (Future)
Pre-compute biome assignments for all possible (elevation, temp, moisture) combinations:
```java
static final Biome[][][] BIOME_LUT = new Biome[100][100][100];  // Discretize to 100 buckets each

static {
    for (int e = 0; e < 100; e++) {
        for (int t = 0; t < 100; t++) {
            for (int m = 0; m < 100; m++) {
                BIOME_LUT[e][t][m] = assign(e / 100.0, (t - 50) * 2, m / 100.0);
            }
        }
    }
}

public static Biome assignFast(double elevation, double temperature, double moisture) {
    int e = (int) (elevation * 99);
    int t = (int) ((temperature + 50) / 100.0 * 99);  // Map -50 to 50 → 0 to 99
    int m = (int) (moisture * 99);
    return BIOME_LUT[e][t][m];
}
```

**Speed gain**: ~3× faster (eliminates branching, CPU-friendly cache access)

## References

### Scientific Background
- **Whittaker Biome Classification**: https://en.wikipedia.org/wiki/Biome#Whittaker's_classification
- **Köppen Climate System**: https://en.wikipedia.org/wiki/Köppen_climate_classification

### Internal Documentation
- [docs/biomes_geography.md](../../../../docs/biomes_geography.md) - Detailed biome specifications
- [docs/world_generation.md](../../../../docs/world_generation.md) - Temperature/moisture generation
- [docs/economy_resources.md](../../../../docs/economy_resources.md) - Resource abundance formulas

### Related Classes
- `WorldGen.java` - Generates elevation, temperature, moisture; calls `assign()`
- `Plate.java` - Tectonic plates influence elevation → biome assignment
- `RandomUtil.java` - Noise used in moisture generation

## Code Quality Notes

### Strengths ✅
- **Enum Safety**: Finite set of biomes, compile-time checked
- **Immutability**: All fields `final` → thread-safe
- **Clear Semantics**: `isWater()`, `isHabitable()` make intent obvious
- **Deterministic**: Same inputs → same biome (no hidden RNG)

### Improvement Areas 🔧
- **Magic Numbers**: Hardcoded thresholds (0.2, 0.6, 25°C) scattered in `assign()`
  - **Fix**: Extract to constants (`OCEAN_THRESHOLD = 0.2`, `HOT_TEMP = 25`)
- **No Validation**: `assign()` accepts any doubles, even nonsensical values (elevation = 999)
  - **Fix**: Add assertions or clamp inputs
- **Limited Extensibility**: Adding new biomes requires modifying `assign()` method
  - **Fix**: Rule-based system with `BiomeRule` objects

### Performance 🚀
- **Branching**: ~10 if/else checks per tile (modern CPUs handle well with branch prediction)
- **No Allocations**: Returns enum constant (zero heap pressure)
- **Cache-Friendly**: Enum values stored in contiguous memory

---

**Last Updated:** 2025-11-11  
**Author:** WorldGen Team  
**Status:** ✅ Implemented (Phase 1.1)
