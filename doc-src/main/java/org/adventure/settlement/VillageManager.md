# VillageManager

**Package:** `org.adventure.settlement`  
**Type:** Service Class (Stateless)

---

## Overview

`VillageManager` detects and manages villages/cities from structure clusters using a DBSCAN-like clustering algorithm. Tracks village growth and automatically promotes villages to towns/cities based on size and composition.

---

## Design Principles

1. **Automatic Detection**: Villages emerge from structure clustering
2. **DBSCAN Clustering**: Structures within radius form clusters
3. **Dynamic Classification**: Settlements evolve from VILLAGE → TOWN → CITY
4. **Clan Governance**: Most common structure owner becomes governing clan
5. **Stateless Service**: All state stored in Village objects

---

## Class Structure

```java
public class VillageManager {
    // Constants
    private static final int VILLAGE_RADIUS = 10;
    private static final int CITY_RADIUS = 20;
    private static final int MIN_STRUCTURES_VILLAGE = 3;
    private static final int MIN_STRUCTURES_TOWN = 15;
    private static final int MIN_STRUCTURES_CITY = 30;
    private static final int MIN_STRUCTURES_CITY_SPECIAL = 20;
    private static final int MIN_POPULATION_CITY = 50;
    
    // No instance state (stateless service)
}
```

---

## Key Methods

### Village Detection
- **`detectVillages(structures)`**: Find all villages in structure list
  - Uses DBSCAN-like clustering with 10-tile radius
  - Minimum 3 structures per cluster
  - Calculates center as average position
  - Classifies village type automatically
  - Generates procedural names
  - Identifies governing clan
  - Returns list of detected Village objects

### Village Updates
- **`updateVillageStatus(village, structures, currentTick)`**: Update village classification
  - Checks if village should be promoted (VILLAGE → TOWN → CITY)
  - Returns true if type changed (promotion occurred)
  - Updates village type in-place if promoted

- **`shouldPromoteToCity(village)`**: Check if village meets city criteria
  - Returns true if village type is CITY
  - Simple check wrapper for clarity

---

## Village Classification System

### VILLAGE (3-14 structures)
- **Criteria**: 3+ structures within 10-tile radius
- **Max Size**: 14 structures (before town promotion)

### TOWN (15-29 structures OR has MARKET)
- **Option 1**: 15-29 structures
- **Option 2**: Any count with MARKET building
- **Special**: Market auto-promotes to town

### CITY (30+ structures OR special)
- **Option 1**: 30+ structures
- **Option 2**: 20+ structures + 50+ NPCs + TEMPLE or GUILD_HALL
- **Highest Tier**: No further promotions

### Classification Logic
```java
private VillageType classifyVillage(List<Structure> structures, int population) {
    int count = structures.size();
    
    // Check CITY criteria first
    if (count >= 30) return CITY;
    if (count >= 20 && population >= 50 && hasSpecialBuilding(structures)) return CITY;
    
    // Check TOWN criteria
    if (count >= 15) return TOWN;
    if (hasMarket(structures)) return TOWN;
    
    // Default to VILLAGE
    return VILLAGE;
}
```

---

## Clustering Algorithm (DBSCAN-like)

### Phase 1: Seed Selection
- Iterate through all structures
- Skip structures already visited
- Start new cluster from unvisited structure

### Phase 2: Cluster Expansion
- Use BFS (breadth-first search) to find neighbors
- Add structures within radius to cluster
- Mark structures as visited when added to queue
- Continue until no more neighbors within radius

### Phase 3: Cluster Validation
- If cluster size >= 3 structures → create Village
- If cluster size < 3 → discard cluster

### Phase 4: Village Creation
- Calculate center: average X and Y of all structures
- Classify type based on structure count and composition
- Generate name using procedural algorithm
- Find governing clan (most common structure owner)
- Create Village object with all data

---

## Name Generation

### Algorithm
```java
String[] prefixes = {"Meadow", "Stone", "River", "Oak", "Silver", "Gold", "Iron", "Copper"};
String[] suffixes = {"dale", "field", "brook", "wood", "vale", "haven", "crest", "ton"};

Random rng = new Random(counter); // Deterministic based on village ID
String name = prefixes[rng.nextInt()] + suffixes[rng.nextInt()];
```

### Examples
- Meadowdale
- Stonefield
- Riverwood
- Goldcrest

---

## Governing Clan Detection

### Algorithm
1. Extract owner IDs from all structures in village
2. Count occurrences of each clan ID
3. Return clan with most structures
4. Return null if no clan owners found

### Handling Ties
- Uses stream API `max()` which returns arbitrary winner on tie
- Future: Could implement tie-breaking rules (oldest clan, richest, etc.)

---

## Usage Examples

### Detect All Villages
```java
VillageManager manager = new VillageManager();
List<Structure> allStructures = region.getStructures();
List<Village> villages = manager.detectVillages(allStructures);
```

### Update Village Status
```java
boolean promoted = manager.updateVillageStatus(village, allStructures, currentTick);
if (promoted) {
    System.out.println(village.getName() + " promoted to " + village.getType());
}
```

### Check for City Promotion
```java
if (manager.shouldPromoteToCity(village)) {
    // Handle city-specific logic
}
```

---

## Implementation Notes

### Simplified Tile Parsing
- Location IDs assumed format: "x_y"
- TODO: Replace with proper TileId parsing when available

### Population Tracking
- Currently passed as parameter (0 in Phase 1.10.2)
- TODO: Calculate from NPC data when NPC system integrated

### Structure Type Checks
- Uses `StructureType` enum for special building detection
- Market, Temple, Guild Hall have specific meanings

---

## Related Classes

- **Village**: Data class representing a settlement
- **VillageType**: Enum for settlement tiers (VILLAGE/TOWN/CITY)
- **Structure**: Buildings that comprise villages
- **StructureType**: Building categories with special village rules

---

## Testing

**Test Classes**: Integration tests in Phase 1.10.2 validation  
**Coverage**: Core detection and classification logic

### Test Scenarios
- Village detection from structure clusters
- Cluster size thresholds (3/15/30)
- Special building promotions (MARKET → TOWN)
- Governing clan detection
- Name generation determinism

---

## Design Decisions

1. **Why DBSCAN-like?**: Naturally finds clusters without predefined count; ideal for emergent settlements.

2. **Why 10-tile radius?**: Matches road generation range; structures this close feel like a settlement.

3. **Why minimum 3 structures?**: 1-2 structures are just buildings; 3+ feels like a community.

4. **Why auto-promotion?**: Villages should evolve naturally as they grow; manual promotion adds tedium.

5. **Why procedural names?**: Generates infinite unique names without manual naming; consistent with world generation.

6. **Why stateless service?**: Simplifies testing and concurrency; all state in Village objects.

---

## Future Enhancements (Post-MVP)

1. **Custom Names**: Allow players to rename villages
2. **Multi-Clan Villages**: Support contested or coalition governance
3. **Village Merging**: Combine nearby villages that grow together
4. **Village Splitting**: Separate distant structure clusters
5. **Village History**: Track founding, growth events, leadership changes

---

## References

- Design: `BUILD_PHASE1.10.x.md` → Phase 1.10.2
- Algorithm: [DBSCAN Clustering](https://en.wikipedia.org/wiki/DBSCAN)
- Related: `Village.md`, `VillageType.md`, `Structure.md`, `RoadGenerator.md`
