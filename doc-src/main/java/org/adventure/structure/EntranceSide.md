# EntranceSide

**Package:** `org.adventure.structure`  
**Type:** Enum  
**Phase:** 1.10.2  
**Status:** ✅ Complete

## Overview

Represents the side of a structure where the entrance is located. Used for placement validation and road generation. The entrance tile must be clear (can be road, cannot be another structure).

## Values

### NORTH
- **Direction:** Top side of structure
- **Offset:** (0, -1) relative to structure center
- **Use Case:** Roads approach from north

### EAST
- **Direction:** Right side of structure
- **Offset:** (+1, 0) relative to structure center
- **Use Case:** Roads approach from east

### SOUTH
- **Direction:** Bottom side of structure
- **Offset:** (0, +1) relative to structure center
- **Use Case:** Roads approach from south (default)

### WEST
- **Direction:** Left side of structure
- **Offset:** (-1, 0) relative to structure center
- **Use Case:** Roads approach from west

## API Reference

### Get Offset
```java
public int[] getOffset()
```

**Returns:** `[dx, dy]` offset from structure center

**Example:**
```java
EntranceSide side = EntranceSide.NORTH;
int[] offset = side.getOffset();  // [0, -1]
```

### Get Entrance Coordinates
```java
public int[] getEntranceCoords(int structureX, int structureY)
```

**Returns:** `[entranceX, entranceY]` absolute coordinates

**Example:**
```java
EntranceSide side = EntranceSide.SOUTH;
int[] coords = side.getEntranceCoords(100, 200);  // [100, 201]
```

## Usage

### Road Connection
```java
Structure house = /* ... */;
EntranceSide entrance = house.getEntrance();

int[] entranceCoords = entrance.getEntranceCoords(
    house.getX(), house.getY());

// Connect road to entrance
RoadGenerator roadGen = /* ... */;
List<RoadTile> roads = roadGen.connectEntranceToRoad(
    entranceCoords[0], entranceCoords[1], house, currentTick);
```

### Placement Validation
```java
StructurePlacementRules rules = /* ... */;
Structure structure = /* ... */;

boolean entranceClear = rules.isEntranceClear(
    structure.getX(), structure.getY(),
    structure.getEntrance(), roadTiles);

if (!entranceClear) {
    System.out.println("Entrance blocked!");
}
```

### Random Entrance Assignment
```java
public EntranceSide randomEntrance(Random rng) {
    EntranceSide[] values = EntranceSide.values();
    return values[rng.nextInt(values.length)];
}
```

## Offset Table

| Side  | Offset (dx, dy) | Example (from 10, 10) |
|-------|-----------------|------------------------|
| NORTH | (0, -1)         | (10, 9)                |
| EAST  | (+1, 0)         | (11, 10)               |
| SOUTH | (0, +1)         | (10, 11)               |
| WEST  | (-1, 0)         | (9, 10)                |

## Integration

### Structure Generation
```java
// Assign entrance during structure creation
Structure structure = new Structure.Builder()
    .locationTileId("100_200")
    .entrance(EntranceSide.SOUTH)
    .build();
```

### Road Pathfinding
```java
// RoadGenerator uses entrance coords for pathfinding
int[] startCoords = structure1.getEntrance()
    .getEntranceCoords(structure1.getX(), structure1.getY());
int[] endCoords = structure2.getEntrance()
    .getEntranceCoords(structure2.getX(), structure2.getY());

List<RoadTile> path = findPath(
    startCoords[0], startCoords[1],
    endCoords[0], endCoords[1]);
```

## Testing

**Test Class:** `org.adventure.EntranceSideTest`  
**Coverage:** 3 tests, 100% coverage

### Test Cases
- ✅ getOffset() returns correct offsets for all sides
- ✅ getEntranceCoords() calculates absolute coordinates
- ✅ All four sides have unique offsets

## Design Decisions

### Why Four Sides Only?
- **Simplicity:** Grid-aligned roads (no diagonals in Phase 1)
- **Clear Placement:** Easy to visualize entrance location
- **Road Generation:** Aligns with 4-direction pathfinding

### Why Default to SOUTH?
- **Visual Convention:** Most buildings depicted with front facing down
- **Road Approach:** Roads typically approach from bottom of screen
- **Player Perspective:** Top-down view expects south-facing entrances

## Related Classes

- `Structure` — Has entrance field
- `RoadGenerator` — Connects to entrances
- `StructurePlacementRules` — Validates entrance clearance

## References

- **Design Doc:** `BUILD_PHASE1.10.x.md` → Phase 1.10.2  
- **Tests:** `src/test/java/org/adventure/EntranceSideTest.java`
