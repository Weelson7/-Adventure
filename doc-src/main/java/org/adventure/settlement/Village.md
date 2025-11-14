# Village

**Package:** `org.adventure.settlement`  
**Type:** Entity Class  
**Phase:** 1.10.2  
**Status:** ✅ Complete

## Overview

`Village` represents a village, town, or city formed from structure clusters. Villages are automatically detected when 3+ structures are within a 10-tile radius. Can be promoted to towns (15+ structures) or cities (30+ structures + population).

## Purpose

- **Settlement Representation:** Entity model for detected structure clusters
- **Type Classification:** Automatic promotion from VILLAGE → TOWN → CITY
- **Population Tracking:** Manages NPC count for promotion criteria
- **Governance:** Tracks controlling clan based on structure ownership
- **Growth Management:** Supports dynamic structure addition/removal

## Key Features

### Village Detection
- **DBSCAN Clustering:** Density-based spatial clustering of structures
- **Automatic Detection:** 3+ structures within 10 tiles = village
- **Center Calculation:** Geometric center (average X/Y of all structures)
- **Name Generation:** Procedural names from prefix+suffix combinations

### Type Classification
| Type    | Requirements                                                     |
|---------|------------------------------------------------------------------|
| VILLAGE | 3-14 structures within 10-tile radius                           |
| TOWN    | 15-29 structures OR has MARKET                                  |
| CITY    | 30+ structures OR (20+ structures + 50+ NPCs + TEMPLE/GUILD)   |

### Dynamic Growth
- **Add Structures:** `addStructure(structureId)` - no duplicates
- **Remove Structures:** `removeStructure(structureId)`
- **Promotion:** Automatic type upgrade via `VillageManager.updateVillageStatus()`
- **Population:** `setPopulation(count)` for NPC tracking

## Data Model

```java
public class Village {
    String id;              // e.g., "village_1"
    String name;            // e.g., "Meadowdale"
    VillageType type;       // VILLAGE, TOWN, CITY
    int centerX, centerY;   // Cluster center
    List<String> structureIds;
    int population;         // NPC count
    String governingClanId; // Most common clan owner
    long foundedTick;
    int schemaVersion;      // v1
}
```

## API Reference

### Constructor (Jackson)
```java
@JsonCreator
public Village(
    String id, String name, VillageType type,
    int centerX, int centerY,
    List<String> structureIds,
    int population, String governingClanId,
    long foundedTick, int schemaVersion)
```

**Validation:**
- `id` cannot be null/empty
- `name` cannot be null/empty
- `type` cannot be null
- `structureIds` must have at least 1 structure
- `population` cannot be negative
- `foundedTick` cannot be negative

### Builder Pattern
```java
Village village = new Village.Builder()
    .id("village_1")
    .name("Meadowdale")
    .type(VillageType.VILLAGE)
    .centerX(100)
    .centerY(200)
    .structureIds(Arrays.asList("structure_1", "structure_2"))
    .population(25)
    .governingClanId("clan_1")
    .foundedTick(1000)
    .build();
```

### Getters
- `String getId()`
- `String getName()`
- `VillageType getType()`
- `int getCenterX()`, `int getCenterY()`
- `List<String> getStructureIds()` — defensive copy
- `int getPopulation()`
- `String getGoverningClanId()`
- `long getFoundedTick()`
- `int getSchemaVersion()`

### Mutable Operations
```java
// Update name
void setName(String name);  // Validates non-null/empty

// Promote/demote type
void setType(VillageType type);  // Validates non-null

// Update population
void setPopulation(int population);  // Validates >= 0

// Change governing clan
void setGoverningClanId(String clanId);

// Structure management
void addStructure(String structureId);     // No duplicates
void removeStructure(String structureId);
```

## Usage Examples

### Detect Villages (via VillageManager)
```java
VillageManager villageManager = new VillageManager();
List<Village> villages = villageManager.detectVillages(structures);

for (Village village : villages) {
    System.out.println("Village: " + village.getName());
    System.out.println("  Type: " + village.getType());
    System.out.println("  Center: (" + village.getCenterX() + 
                       ", " + village.getCenterY() + ")");
    System.out.println("  Structures: " + village.getStructureIds().size());
}
```

### Promote Village
```java
Village village = /* ... */;
village.addStructure("new_structure_id");

VillageManager manager = new VillageManager();
boolean promoted = manager.updateVillageStatus(village, allStructures, currentTick);

if (promoted) {
    System.out.println(village.getName() + " promoted to " + village.getType());
}
```

### Manual Promotion
```java
Village village = /* ... */;
village.setType(VillageType.CITY);
village.setName("Great City of " + village.getName());
```

## Integration

### WorldGen
```java
// Phase 11.6: Detect villages from structure clusters
VillageManager villageManager = new VillageManager();
List<Village> villages = villageManager.detectVillages(structures);
```

### Persistence
- **Serialization:** Jackson JSON format
- **Schema Version:** 1 (current)
- **File:** `world_villages.json` (planned)
- **Format:**
  ```json
  {
    "id": "village_1",
    "name": "Meadowdale",
    "type": "VILLAGE",
    "centerX": 100,
    "centerY": 200,
    "structureIds": ["structure_1", "structure_2", "structure_3"],
    "population": 25,
    "governingClanId": "clan_1",
    "foundedTick": 1000,
    "schemaVersion": 1
  }
  ```

### Region Simulation
- Villages tracked per region for efficient spatial queries
- Population updates from NPC lifecycle events
- Structure additions trigger promotion checks

## Testing

**Test Class:** `org.adventure.VillageTest`  
**Coverage:** 13 tests, 100% line coverage

### Test Categories
- **Builder Pattern:** Valid construction, required fields, validation
- **Structure Management:** Add, remove, duplicates, nonexistent
- **Mutable Fields:** setName, setPopulation, setType validation
- **Schema Version:** Default value handling

## Design Decisions

### Why DBSCAN Clustering?
- **Arbitrary Shapes:** Villages don't have to be perfect circles
- **Natural Boundaries:** Handles irregular structure distributions
- **Scalable:** O(n²) worst case, O(n log n) with spatial indexing

### Why 10-Tile Radius?
- **Gameplay Balance:** Large enough for multi-building settlements
- **Performance:** Small enough for efficient neighbor searches
- **Visual Clarity:** Players can see entire village on screen

### Why Three-Tier System (Village/Town/City)?
- **Clear Progression:** Natural growth path for settlements
- **Mechanical Significance:** Different tiers unlock different features (Phase 2.x)
- **Player Recognition:** Easy to understand hierarchy

## Future Enhancements (Phase 2.x)

- **Village Reputation:** Faction standing affects prices, quests
- **Mayor System:** Elected/appointed leader with governance powers
- **Village Walls:** Defensive structures for cities (50+ structures)
- **Trade Routes:** Villages connected via major roads = trade bonuses
- **Village Quests:** Settlement-specific objectives (build X, defend from Y)

## Related Classes

- `VillageManager` — Detection and promotion logic
- `VillageType` — Enum for VILLAGE/TOWN/CITY
- `Settlement` — Pre-planned settlements (Phase 1.10.1)
- `Structure` — Individual buildings
- `RoadGenerator` — Connects villages via roads

## References

- **Design Doc:** `BUILD_PHASE1.10.x.md` → Phase 1.10.2
- **Specs:** `docs/specs_summary.md` → Village criteria
- **Tests:** `src/test/java/org/adventure/VillageTest.java`
