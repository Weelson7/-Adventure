# VillageManager

**Package:** `org.adventure.settlement`  
**Type:** Manager Class  
**Phase:** 1.10.2  
**Status:** ✅ Complete

## Overview

`VillageManager` detects and manages villages/cities from structure clusters using a DBSCAN-like clustering algorithm. Tracks village growth and promotes villages to towns/cities based on structure count and population.

## Purpose

- **Village Detection:** Identify structure clusters (3+ buildings within 10 tiles)
- **Automatic Promotion:** Upgrade VILLAGE → TOWN → CITY based on growth
- **Status Tracking:** Monitor village populations and structure counts
- **Clan Assignment:** Determine governing clan from structure ownership

## Key Features

### DBSCAN Clustering Algorithm
```java
1. For each unvisited structure:
   2. Find neighbors within RADIUS (10 tiles)
   3. If cluster has MIN_STRUCTURES (3+):
      - Breadth-first search to expand cluster
      - Calculate center (average X/Y)
      - Classify type (VILLAGE/TOWN/CITY)
      - Generate name, assign governing clan
   4. Mark all cluster structures as visited
```

### Village Classification
| Type    | Criteria                                                         |
|---------|------------------------------------------------------------------|
| VILLAGE | 3-14 structures within 10 tiles                                 |
| TOWN    | 15-29 structures OR has MARKET                                  |
| CITY    | 30+ structures OR (20+ structures + 50+ NPCs + TEMPLE/GUILD)   |

### Constants
```java
VILLAGE_RADIUS = 10           // Detection radius
CITY_RADIUS = 20              // (Future use)
MIN_STRUCTURES_VILLAGE = 3    // Minimum for village
MIN_STRUCTURES_TOWN = 15      // Automatic town promotion
MIN_STRUCTURES_CITY = 30      // Automatic city promotion
MIN_STRUCTURES_CITY_SPECIAL = 20  // With special buildings
MIN_POPULATION_CITY = 50      // Minimum population for city
```

## API Reference

### Constructor
```java
public VillageManager()
```
No dependencies, stateless operation.

### Detect Villages
```java
public List<Village> detectVillages(List<Structure> structures)
```

**Algorithm:**
1. Create spatial index (simple map for now)
2. Initialize visited set
3. For each unvisited structure:
   - Find cluster via BFS
   - If cluster.size() >= 3 → create Village
   - Calculate center, classify type, generate name
4. Return all detected villages

**Returns:** List of `Village` instances (empty if none found)

**Example:**
```java
VillageManager manager = new VillageManager();
List<Village> villages = manager.detectVillages(allStructures);

System.out.println("Found " + villages.size() + " villages");
```

### Update Village Status
```java
public boolean updateVillageStatus(Village village, 
                                   List<Structure> structures, 
                                   long currentTick)
```

**Purpose:** Check for type promotions (VILLAGE → TOWN → CITY)

**Returns:** `true` if village was promoted, `false` otherwise

**Example:**
```java
for (Village village : villages) {
    boolean promoted = manager.updateVillageStatus(village, structures, currentTick);
    if (promoted) {
        System.out.println(village.getName() + " promoted to " + 
                           village.getType());
    }
}
```

### Check City Promotion
```java
public boolean shouldPromoteToCity(Village village)
```

**Returns:** `true` if village meets city criteria

**Criteria:**
- Village type is already CITY (no further promotion)

## Internal Methods

### Find Cluster (BFS)
```java
private List<Structure> findCluster(
    Structure start, 
    List<Structure> allStructures, 
    Set<String> visited, 
    int radius)
```

**Algorithm:**
1. Initialize queue with start structure
2. Mark start as visited
3. While queue not empty:
   - Poll current structure
   - Find all unvisited neighbors within radius
   - Add neighbors to queue, mark visited
4. Return cluster

### Classify Village
```java
private VillageType classifyVillage(List<Structure> structures, int population)
```

**Logic:**
```
if (structureCount >= 30) → CITY
if (structureCount >= 20 && population >= 50 && hasTemple/GuildHall) → CITY
if (structureCount >= 15) → TOWN
if (hasMarket) → TOWN
else → VILLAGE
```

### Name Generation
```java
private String generateVillageName(int counter, VillageType type)
```

**Algorithm:**
- Prefix: ["Meadow", "Stone", "River", "Oak", "Silver", "Gold", "Iron", "Copper"]
- Suffix: ["dale", "field", "brook", "wood", "vale", "haven", "crest", "ton"]
- Combine: `prefix + suffix` (e.g., "Meadowdale", "Stonebrook")
- Seeded from counter for determinism

### Governing Clan
```java
private String findGoverningClan(List<Structure> structures)
```

**Logic:** Find most common clan ID among structure owners

## Usage Examples

### Detect All Villages
```java
// After worldgen structures are placed
WorldGen gen = new WorldGen(512, 512);
gen.generate(12345L);

VillageManager manager = new VillageManager();
List<Village> villages = manager.detectVillages(gen.getStructures());

for (Village v : villages) {
    System.out.println(v.getName() + " (" + v.getType() + 
                       ") - " + v.getStructureIds().size() + " structures");
}
```

### Track Village Growth
```java
VillageManager manager = new VillageManager();
List<Village> villages = /* ... */;

// Each tick, check for promotions
void onTick(long currentTick) {
    for (Village village : villages) {
        boolean promoted = manager.updateVillageStatus(
            village, allStructures, currentTick);
        
        if (promoted) {
            // Trigger promotion event
            Event event = new Event.Builder()
                .title(village.getName() + " promoted!")
                .description("The settlement has grown to a " + village.getType())
                .build();
            eventSystem.triggerEvent(event);
        }
    }
}
```

### Find Villages Near Location
```java
public List<Village> findVillagesNear(int x, int y, int radius, 
                                      List<Village> allVillages) {
    return allVillages.stream()
        .filter(v -> {
            double dist = Math.sqrt(
                Math.pow(x - v.getCenterX(), 2) + 
                Math.pow(y - v.getCenterY(), 2));
            return dist <= radius;
        })
        .collect(Collectors.toList());
}
```

## Integration

### WorldGen (Phase 11.6)
```java
// Generate structures first
List<Structure> structures = generateStructures(settlements, seed);

// Then detect villages
VillageManager villageManager = new VillageManager();
List<Village> villages = villageManager.detectVillages(structures);

// Store for later use
this.villages = villages;
```

### Region Simulation
```java
// Track villages per region
Map<String, List<Village>> villagesByRegion = /* ... */;

// Update village populations from NPCs
void updateVillagePopulations() {
    for (Village village : villages) {
        int population = npcs.stream()
            .filter(npc -> village.getStructureIds()
                .contains(npc.getHomeStructureId()))
            .count();
        village.setPopulation(population);
    }
}
```

## Performance

### Time Complexity
- **Detection:** O(n² / k) where k = spatial buckets
  - Worst case: O(n²) for dense clusters
  - Best case: O(n log n) with spatial indexing
- **Update:** O(1) per village
- **Cluster Finding:** O(n) BFS per starting structure

### Space Complexity
- **Visited Set:** O(n) for structure tracking
- **Cluster List:** O(n) per cluster
- **Total:** O(n) where n = structure count

### Optimization Opportunities (Phase 2.x)
- **Spatial Index:** Grid-based bucketing for O(n log n) detection
- **Incremental Updates:** Only re-detect changed regions
- **Caching:** Store cluster centers for fast proximity queries

## Testing

**Test Class:** (Planned) `org.adventure.VillageManagerTest`  
**Coverage:** Integration tests in WorldGenTest

### Test Cases (Planned)
- ✅ Detect villages from 3+ structure clusters
- ✅ Ignore isolated structures (< 3 in range)
- ✅ Correctly calculate cluster centers
- ✅ Promote VILLAGE → TOWN at 15 structures
- ✅ Promote TOWN → CITY at 30 structures
- ✅ Promote TOWN with MARKET regardless of count
- ✅ Assign governing clan (most common owner)
- ✅ Generate unique village names

## Design Decisions

### Why DBSCAN?
- **Handles Arbitrary Shapes:** Villages don't have to be circular
- **No Pre-defined K:** Automatic cluster count
- **Noise Handling:** Isolated structures ignored

### Why BFS for Clustering?
- **Guaranteed Connectivity:** All structures reachable from start
- **Simple Implementation:** Easy to debug and test
- **Efficient:** O(n) per cluster

### Why Not K-Means?
- **Requires Pre-defined K:** Don't know village count in advance
- **Circular Clusters:** Assumes spherical distributions
- **Sensitive to Outliers:** Isolated structures affect centers

## Future Enhancements (Phase 2.x)

- **Spatial Indexing:** Quadtree or grid for O(log n) queries
- **Village Merging:** Adjacent villages can merge into cities
- **Village Splitting:** Civil wars split cities into smaller settlements
- **Dynamic Naming:** Villages renamed after major events
- **Historical Tracking:** Record village founding, growth, decline

## Related Classes

- `Village` — Village entity model
- `VillageType` — VILLAGE/TOWN/CITY enum
- `Structure` — Individual buildings
- `Settlement` — Pre-planned settlements (Phase 1.10.1)
- `RoadGenerator` — Connects villages with roads

## References

- **Design Doc:** `BUILD_PHASE1.10.x.md` → Phase 1.10.2
- **Algorithm:** DBSCAN (Density-Based Spatial Clustering)
- **Specs:** `docs/specs_summary.md` → Village detection criteria
