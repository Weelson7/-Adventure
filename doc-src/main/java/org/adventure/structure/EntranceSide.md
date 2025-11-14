# EntranceSide

**Package:** `org.adventure.structure`  
**Type:** Enum

---

## Overview

`EntranceSide` represents the side of a structure where the entrance is located. Used for placement validation, road generation, and structure orientation. The entrance tile must be clear (can be road, cannot be another structure).

---

## Enum Values

### NORTH
- **Description**: Entrance on north side (top)
- **Offset**: (0, -1) relative to structure center
- **Visual**: Entrance faces upward

### EAST
- **Description**: Entrance on east side (right)
- **Offset**: (1, 0) relative to structure center
- **Visual**: Entrance faces rightward

### SOUTH
- **Description**: Entrance on south side (bottom)
- **Offset**: (0, 1) relative to structure center
- **Visual**: Entrance faces downward

### WEST
- **Description**: Entrance on west side (left)
- **Offset**: (-1, 0) relative to structure center
- **Visual**: Entrance faces leftward

---

## Key Methods

### Offset Calculation
- **`getOffset()`**: Get [dx, dy] offset from structure center
  - Returns `int[2]` array
  - NORTH: [0, -1]
  - EAST: [1, 0]
  - SOUTH: [0, 1]
  - WEST: [-1, 0]

### Coordinate Calculation
- **`getEntranceCoords(structureX, structureY)`**: Calculate entrance coordinates
  - Takes structure center position
  - Returns `int[2]` array [entranceX, entranceY]
  - Applies offset to structure position
  - Example: SOUTH entrance at (10, 20) → [10, 21]

---

## Coordinate System

### World Coordinates
- **X-axis**: Positive is East (right)
- **Y-axis**: Positive is South (down)
- **Origin**: Top-left corner (0, 0)

### Offset Directions
```
        NORTH (0, -1)
             ↑
WEST (-1, 0) ← @ → (+1, 0) EAST
             ↓
        SOUTH (0, +1)
```

---

## Usage Examples

### Get Entrance Offset
```java
int[] offset = EntranceSide.SOUTH.getOffset();
// offset = [0, 1]
```

### Calculate Entrance Position
```java
Structure structure = ...; // at (10, 20)
EntranceSide entrance = structure.getEntrance();
int[] entrancePos = entrance.getEntranceCoords(10, 20);
// If entrance is SOUTH: entrancePos = [10, 21]
```

### Check All Directions
```java
for (EntranceSide side : EntranceSide.values()) {
    int[] coords = side.getEntranceCoords(structureX, structureY);
    System.out.println(side + ": " + Arrays.toString(coords));
}
```

### Validate Entrance Clearance
```java
EntranceSide entrance = structure.getEntrance();
int[] entrancePos = entrance.getEntranceCoords(structureX, structureY);
boolean isClear = isTerrainClear(entrancePos[0], entrancePos[1]);
```

---

## Integration Points

### Structure Placement
- **StructurePlacementRules**: Validates entrance clearance
  - Entrance tile must not be blocked by structure
  - Entrance tile can be road (preferred)
  - Entrance tile must have valid terrain

### Road Generation
- **RoadGenerator**: Connects roads to entrances
  - Roads path from entrance to entrance
  - Entrance-adjacent roads allow direct access

### Structure Creation
- **Structure.Builder**: Accepts EntranceSide parameter
  - Default: SOUTH if not specified
  - Stored with structure for persistence

---

## Validation Rules

### Entrance Placement
1. **Clearance**: 1 tile in front must be clear or road
2. **Terrain**: Entrance tile must be buildable (not water/mountain)
3. **Not Blocked**: No structure can occupy entrance tile
4. **Within Bounds**: Entrance coordinates must be in world

---

## Related Classes

- **Structure**: Uses EntranceSide for entrance orientation
- **StructurePlacementRules**: Validates entrance clearance
- **RoadGenerator**: Connects roads to entrance coordinates
- **RoadTile**: Can occupy entrance tiles (but not structure tile)

---

## Testing

**Test Class**: `EntranceSideTest.java`  
**Test Count**: 10 tests  
**Coverage**: 100%

### Test Categories
- Offset calculations (4 tests)
- Coordinate generation (5 tests)
- Distinctness validation (1 test)

---

## Design Decisions

1. **Why four cardinal directions only?**: Simplifies placement and road generation; sufficient for Phase 1.10.2.

2. **Why enum not class?**: Fixed set of directions; no need for extensibility at this level.

3. **Why offset from center?**: Structures placed at center coordinate; offset calculates entrance from that.

4. **Why [dx, dy] array not Point class?**: Minimal dependency; simple primitive array sufficient.

5. **Why Y-positive is South?**: Matches common screen coordinates (top = 0, down = positive).

---

## Future Enhancements (Post-MVP)

1. **Diagonal Entrances**: NE, NW, SE, SW for corner entrances
2. **Multiple Entrances**: Allow structures with 2+ entrances
3. **Entrance Width**: Support multi-tile entrances for large buildings
4. **Entrance Restrictions**: Locked entrances, faction-specific access
5. **Visual Indicators**: Render entrance markers on map

---

## References

- Design: `BUILD_PHASE1.10.x.md` → Phase 1.10.2
- Related: `Structure.md`, `StructurePlacementRules.md`, `RoadGenerator.md`
- Tests: `EntranceSideTest.md`
