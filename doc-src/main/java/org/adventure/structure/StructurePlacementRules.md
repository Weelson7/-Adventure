# StructurePlacementRules

**Package:** `org.adventure.structure`  
**Type:** Service Class (Stateful)

---

## Overview

`StructurePlacementRules` validates structure placement according to game rules. Ensures proper spacing between structures, entrance clearance, terrain suitability, and prevents road collisions. Uses a simplified terrain model for Phase 1.10.2.

---

## Design Principles

1. **Rule-Based Validation**: Clear, documented placement rules
2. **Multiple Error Reporting**: Returns all errors, not just first one
3. **Terrain-Aware**: Uses elevation map for water/mountain detection
4. **Entrance Validation**: Ensures entrances remain accessible
5. **Stateful Service**: Holds reference to elevation map for efficiency

---

## Class Structure

```java
public class StructurePlacementRules {
    // Constants
    private static final int MIN_STRUCTURE_SPACING = 5;
    private static final double WATER_THRESHOLD = 0.2;
    private static final double MOUNTAIN_THRESHOLD = 0.7;
    
    // State
    private final double[][] elevationMap;
    private final int worldWidth;
    private final int worldHeight;
}
```

---

## Placement Rules

### Rule 1: Minimum Spacing (5 tiles)
- **Requirement**: Structures must be at least 5 tiles apart (center to center)
- **Calculation**: Euclidean distance between structure centers
- **Error Type**: `TOO_CLOSE_TO_STRUCTURE`

### Rule 2: Entrance Clearance (1 tile)
- **Requirement**: 1 tile in front of entrance must be clear or road
- **Clear Means**: Not occupied by another structure, valid terrain
- **Road Allowed**: Entrance can face/touch road tiles
- **Error Type**: `BLOCKING_ENTRANCE`

### Rule 3: No Building on Roads
- **Requirement**: Structure center cannot be on existing road tile
- **Exception**: Entrance can touch road (not structure center)
- **Error Type**: `ON_ROAD`

### Rule 4: Terrain Validation
- **Water Check**: Elevation < 0.2 (unless water structure like DOCK)
- **Mountain Check**: Elevation > 0.7 (too steep for building)
- **Error Type**: `UNSUITABLE_TERRAIN`

### Rule 5: Bounds Checking
- **Requirement**: Structure must be within world dimensions
- **Fatal Error**: Stops further validation if out of bounds
- **Error Type**: `OUT_OF_BOUNDS`

---

## Key Methods

### Validation
- **`canPlaceStructure(x, y, type, entrance, existingStructures, roadTiles)`**: Quick boolean check
  - Returns true if placement is valid (no errors)
  - Returns false if any errors exist
  - Calls `validatePlacement()` internally

- **`validatePlacement(x, y, type, entrance, existingStructures, roadTiles)`**: Full validation
  - Returns `List<PlacementError>` (empty if valid)
  - Reports all errors found (not just first one)
  - Checks all rules in order
  - Defaults entrance to SOUTH if null

### Entrance Validation
- **`isEntranceClear(x, y, entrance, roadTiles)`**: Check entrance clearance only
  - Returns true if entrance is clear or connects to road
  - Returns false if entrance is blocked or has invalid terrain
  - Useful for road generation logic

---

## Validation Flow

### Phase 1: Fatal Errors
1. Check bounds (OUT_OF_BOUNDS)
2. If out of bounds, return immediately (no further checks)

### Phase 2: Terrain Checks
1. Check mountain terrain (elevation > 0.7)
2. Check water terrain (elevation < 0.2, unless water structure)

### Phase 3: Collision Checks
1. Check road collision (structure on road tile)
2. Check structure proximity (within 5 tiles of existing)

### Phase 4: Entrance Checks
1. Calculate entrance coordinates
2. Check entrance terrain (not water/mountain)
3. Check entrance structure collision
4. Validate entrance is within bounds

---

## Terrain Model (Phase 1.10.2)

### Simplified Elevation Map
- **Type**: `double[][]` (2D array)
- **Range**: 0.0 to 1.0
- **Interpretation**:
  - 0.0 - 0.2: Water (deep → shallow)
  - 0.2 - 0.7: Buildable land
  - 0.7 - 1.0: Mountains (foothills → peaks)

### Future Integration
- Full terrain integration with Tile class (Phase 2)
- Real terrain types (OCEAN, PLAINS, HILLS, MOUNTAINS)
- Biome-specific building rules

---

## Water Structures

### Special Handling
- **DOCK**: Can be placed in water (elevation < 0.2)
- **FISHING_HUT**: Can be placed in water
- **Others**: Cannot be placed in water

### Implementation
```java
private boolean isWaterStructure(StructureType type) {
    return type == StructureType.DOCK || type == StructureType.FISHING_HUT;
}
```

---

## Usage Examples

### Simple Validation Check
```java
StructurePlacementRules rules = new StructurePlacementRules(elevationMap);
boolean canPlace = rules.canPlaceStructure(
    10, 20, 
    StructureType.HOUSE, 
    EntranceSide.SOUTH, 
    existingStructures, 
    roadTiles
);
```

### Get All Errors
```java
List<PlacementError> errors = rules.validatePlacement(
    10, 20, 
    StructureType.HOUSE, 
    EntranceSide.SOUTH, 
    existingStructures, 
    roadTiles
);

if (!errors.isEmpty()) {
    for (PlacementError error : errors) {
        System.err.println(error);
    }
}
```

### Check Entrance Only
```java
boolean entranceClear = rules.isEntranceClear(
    10, 20, 
    EntranceSide.SOUTH, 
    roadTiles
);
```

### Water Structure Placement
```java
boolean canPlaceDock = rules.canPlaceStructure(
    5, 15, 
    StructureType.DOCK,  // Water structure
    EntranceSide.NORTH, 
    existingStructures, 
    roadTiles
);
// Allows placement in water
```

---

## Helper Methods

### Coordinate Parsing
- **`parseX(locationTileId)`**: Extract X from "x_y" format
- **`parseY(locationTileId)`**: Extract Y from "x_y" format
- Error handling: Returns 0 on parse failure

### Bounds Checking
- **`isWithinBounds(x, y)`**: Check if coordinates are valid
- Returns true if 0 <= x < worldWidth AND 0 <= y < worldHeight

### Structure Type Checks
- **`isWaterStructure(type)`**: Check if structure can be in water
- Returns true for DOCK, FISHING_HUT

---

## Related Classes

- **PlacementError**: Error report with type and message
- **PlacementErrorType**: Enum of error categories
- **EntranceSide**: Entrance direction for clearance checks
- **Structure**: Buildings being placed
- **RoadTile**: Roads that structures can't overlap (except entrance)

---

## Testing

**Test Classes**: Integration tests in Phase 1.10.2 validation  
**Coverage**: All placement rules and error types

### Test Scenarios
- Valid placement (no errors)
- Each error type individually
- Multiple errors in single validation
- Entrance clearance with roads
- Water structure special cases
- Bounds checking edge cases

---

## Design Decisions

1. **Why 5-tile minimum spacing?**: Prevents cluttered building; allows room for roads and expansion.

2. **Why report all errors?**: Helps players understand all issues at once; avoids trial-and-error fixing.

3. **Why separate entrance clearance method?**: Road generation needs this check without full validation.

4. **Why water structures exception?**: Docks/fishing huts logically belong near water; adds gameplay variety.

5. **Why elevation thresholds 0.2/0.7?**: Provides ~50% buildable land; matches typical world generation.

---

## Future Enhancements (Post-MVP)

1. **Dynamic Spacing**: Larger structures require more space
2. **Terrain Type Rules**: Per-biome building restrictions
3. **Slope Validation**: Prevent building on steep terrain
4. **Resource Checks**: Require resources in inventory for placement
5. **Permission Validation**: Check clan/faction ownership rules

---

## References

- Design: `BUILD_PHASE1.10.x.md` → Phase 1.10.2
- Related: `PlacementError.md`, `PlacementErrorType.md`, `EntranceSide.md`, `Structure.md`
- Specs: `docs/specs_summary.md` → Structure Placement Rules
