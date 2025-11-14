# StructurePlacementRules

**Package:** `org.adventure.structure`  
**Type:** Validator Class  
**Phase:** 1.10.2  
**Status:** ✅ Complete

## Overview

Validates structure placement according to game rules. Checks spacing, entrance clearance, terrain suitability, road conflicts, and world bounds.

## Placement Rules

1. **Minimum Spacing:** 5 tiles between structure centers
2. **Entrance Clearance:** 1 tile in front must be clear or road
3. **No Building on Roads:** Structure cannot occupy road tile (entrance can touch)
4. **Terrain Validation:** elevation 0.2-0.7 (not water/mountains unless special)
5. **Bounds Checking:** Within world dimensions

## Constants

```java
MIN_STRUCTURE_SPACING = 5
WATER_THRESHOLD = 0.2
MOUNTAIN_THRESHOLD = 0.7
```

## API Reference

### Constructor
```java
public StructurePlacementRules(double[][] elevationMap)
```

**Parameters:**
- `elevationMap` — 2D elevation array (0.0 = water, 1.0 = peaks)

### Can Place Structure
```java
public boolean canPlaceStructure(
    int x, int y, 
    StructureType type, 
    EntranceSide entrance,
    List<Structure> existingStructures, 
    Map<String, RoadTile> roadTiles)
```

**Returns:** `true` if placement is valid (no errors)

### Validate Placement
```java
public List<PlacementError> validatePlacement(
    int x, int y,
    StructureType type,
    EntranceSide entrance,
    List<Structure> existingStructures,
    Map<String, RoadTile> roadTiles)
```

**Returns:** List of errors (empty if valid)

**Validation Order:**
1. Bounds check (fatal, returns immediately)
2. Terrain check (water/mountains)
3. Road conflict check
4. Spacing check (5-tile minimum)
5. Entrance clearance check

### Is Entrance Clear
```java
public boolean isEntranceClear(
    int x, int y,
    EntranceSide entrance,
    Map<String, RoadTile> roadTiles)
```

**Returns:** `true` if entrance is clear or connects to road

## Usage Examples

### Basic Validation
```java
StructurePlacementRules rules = new StructurePlacementRules(elevationMap);

List<PlacementError> errors = rules.validatePlacement(
    100, 200, StructureType.HOUSE, EntranceSide.SOUTH,
    existingStructures, roadTiles);

if (errors.isEmpty()) {
    System.out.println("Valid placement!");
} else {
    System.out.println("Cannot place structure:");
    errors.forEach(e -> System.out.println("  - " + e));
}
```

### Quick Check
```java
StructurePlacementRules rules = new StructurePlacementRules(elevationMap);

boolean canPlace = rules.canPlaceStructure(
    100, 200, StructureType.HOUSE, EntranceSide.SOUTH,
    existingStructures, roadTiles);

if (canPlace) {
    // Create structure
}
```

### Entrance Validation
```java
StructurePlacementRules rules = new StructurePlacementRules(elevationMap);

if (rules.isEntranceClear(100, 201, EntranceSide.SOUTH, roadTiles)) {
    System.out.println("Entrance accessible");
}
```

## Validation Details

### Bounds Checking
```java
if (x < 0 || x >= worldWidth || y < 0 || y >= worldHeight) {
    errors.add(OUT_OF_BOUNDS);
    return errors;  // Fatal error
}
```

### Terrain Validation
```java
double elevation = elevationMap[y][x];

if (elevation > 0.7) {
    errors.add(UNSUITABLE_TERRAIN, "mountain");
}

if (elevation < 0.2 && !isWaterStructure(type)) {
    errors.add(UNSUITABLE_TERRAIN, "water");
}
```

### Water Structures
```java
private boolean isWaterStructure(StructureType type) {
    return type == DOCK || type == FISHING_HUT;
}
```

### Spacing Check
```java
for (Structure existing : existingStructures) {
    double distance = Math.sqrt(
        Math.pow(x - existing.getX(), 2) +
        Math.pow(y - existing.getY(), 2));
    
    if (distance < 5) {
        errors.add(TOO_CLOSE_TO_STRUCTURE);
    }
}
```

### Entrance Clearance
```java
int[] entranceCoords = entrance.getEntranceCoords(x, y);
int ex = entranceCoords[0];
int ey = entranceCoords[1];

// Check if blocked by structure
boolean blocked = existingStructures.stream()
    .anyMatch(s -> s.getX() == ex && s.getY() == ey);

// Check entrance terrain
double entranceElevation = elevationMap[ey][ex];
if (entranceElevation < 0.2 || entranceElevation > 0.7) {
    errors.add(BLOCKING_ENTRANCE);
}
```

## Integration

### Structure Generation
```java
StructurePlacementRules rules = new StructurePlacementRules(elevationMap);

for (int attempt = 0; attempt < maxAttempts; attempt++) {
    int x = rng.nextInt(worldWidth);
    int y = rng.nextInt(worldHeight);
    
    if (rules.canPlaceStructure(x, y, type, entrance, 
                                 existingStructures, roadTiles)) {
        Structure structure = createStructure(x, y);
        existingStructures.add(structure);
        break;
    }
}
```

### Settlement Generator
```java
// Validate before creating settlement structures
List<PlacementError> errors = rules.validatePlacement(
    settlementX, settlementY, StructureType.GUILD_HALL,
    EntranceSide.SOUTH, existingStructures, roadTiles);

if (!errors.isEmpty()) {
    // Try different location
    settlementX += 10;
    settlementY += 10;
}
```

## Testing

**Test Class:** (Planned) `org.adventure.StructurePlacementRulesTest`  
**Coverage:** Integration tests in SettlementGenerator

### Test Cases (Planned)
- ✅ Reject structures too close (< 5 tiles)
- ✅ Reject structures on mountains (elevation > 0.7)
- ✅ Reject structures in water (elevation < 0.2) unless special
- ✅ Reject structures out of bounds
- ✅ Reject structures on roads
- ✅ Reject structures with blocked entrances
- ✅ Accept valid placements
- ✅ Accept water structures in water

## Design Decisions

### Why 5-Tile Minimum Spacing?
- **Visual Clarity:** Structures clearly separated
- **Road Space:** Room for roads between buildings
- **Performance:** Limits neighbor checks

### Why Entrance Must Be Clear?
- **NPC Access:** NPCs must pathfind to entrance
- **Road Connection:** Roads must reach entrance
- **Logical Consistency:** Can't enter blocked building

### Why Allow Structures Adjacent to Roads?
- **Realistic:** Buildings typically face roads
- **Entrance Access:** Entrance tile can touch road
- **Layout Flexibility:** Compact settlements possible

## Future Enhancements (Phase 2.x)

- **Tile System Integration:** Use actual Tile objects instead of elevation array
- **Zone Restrictions:** Residential/commercial/industrial zones
- **Player Placement:** Different rules for player-built vs auto-generated
- **Upgrade Validation:** Check if structure can be upgraded in place
- **Demolition Rules:** Validate safe demolition (NPCs evacuated)

## Related Classes

- `PlacementError` — Error messages
- `PlacementErrorType` — Error categories
- `Structure` — Buildings being placed
- `EntranceSide` — Entrance validation
- `RoadTile` — Road conflict checking

## References

- **Design Doc:** `BUILD_PHASE1.10.x.md` → Phase 1.10.2  
- **Specs:** `docs/specs_summary.md` → Placement rules
