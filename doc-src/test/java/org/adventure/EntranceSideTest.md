# EntranceSideTest

**Package:** `org.adventure` (test)  
**Type:** JUnit 5 Test Class  
**Tests:** EntranceSide enum

---

## Overview

`EntranceSideTest` validates the `EntranceSide` enum, including offset calculations and entrance coordinate generation. Ensures all four cardinal directions work correctly.

---

## Test Categories

### Offset Calculation Tests (4 tests)

#### `testNorthOffset()`
- **Purpose**: Verify NORTH offset is (0, -1)
- **Approach**: Call NORTH.getOffset()
- **Assertions**: assertArrayEquals([0, -1], offset)

#### `testEastOffset()`
- **Purpose**: Verify EAST offset is (1, 0)
- **Approach**: Call EAST.getOffset()
- **Assertions**: assertArrayEquals([1, 0], offset)

#### `testSouthOffset()`
- **Purpose**: Verify SOUTH offset is (0, 1)
- **Approach**: Call SOUTH.getOffset()
- **Assertions**: assertArrayEquals([0, 1], offset)

#### `testWestOffset()`
- **Purpose**: Verify WEST offset is (-1, 0)
- **Approach**: Call WEST.getOffset()
- **Assertions**: assertArrayEquals([-1, 0], offset)

---

### Coordinate Generation Tests (5 tests)

#### `testGetEntranceCoordsNorth()`
- **Purpose**: Verify NORTH entrance calculation
- **Approach**: NORTH.getEntranceCoords(10, 20)
- **Expected**: [10, 19] (same X, Y-1)
- **Rationale**: North means upward (-Y direction)

#### `testGetEntranceCoordsEast()`
- **Purpose**: Verify EAST entrance calculation
- **Approach**: EAST.getEntranceCoords(10, 20)
- **Expected**: [11, 20] (X+1, same Y)
- **Rationale**: East means rightward (+X direction)

#### `testGetEntranceCoordsSouth()`
- **Purpose**: Verify SOUTH entrance calculation
- **Approach**: SOUTH.getEntranceCoords(10, 20)
- **Expected**: [10, 21] (same X, Y+1)
- **Rationale**: South means downward (+Y direction)

#### `testGetEntranceCoordsWest()`
- **Purpose**: Verify WEST entrance calculation
- **Approach**: WEST.getEntranceCoords(10, 20)
- **Expected**: [9, 20] (X-1, same Y)
- **Rationale**: West means leftward (-X direction)

#### `testGetEntranceCoordsAtOrigin()`
- **Purpose**: Test edge case at (0, 0)
- **Approach**: SOUTH.getEntranceCoords(0, 0)
- **Expected**: [0, 1]
- **Rationale**: Verify no special handling needed for origin

---

### Distinctness Test (1 test)

#### `testAllDirectionsDistinct()`
- **Purpose**: Verify all four offsets are unique
- **Approach**: Get all offsets, compare pairwise
- **Assertions**: No two offsets are equal
- **Rationale**: Ensures each direction is truly distinct

---

## Test Data

### Test Positions
- **Standard**: (10, 20) - typical structure position
- **Origin**: (0, 0) - edge case at world origin

### Expected Offsets
- **NORTH**: [0, -1]
- **EAST**: [1, 0]
- **SOUTH**: [0, 1]
- **WEST**: [-1, 0]

### Expected Entrance Coordinates (from 10, 20)
- **NORTH**: [10, 19]
- **EAST**: [11, 20]
- **SOUTH**: [10, 21]
- **WEST**: [9, 20]

---

## Coordinate System Validation

### Axis Orientation
- **X-axis**: Positive is East (right)
- **Y-axis**: Positive is South (down)
- **Origin**: Top-left corner (0, 0)

### Direction Mapping
```
        NORTH (0, -1)
             ↑
WEST (-1, 0) ← @ → (+1, 0) EAST
             ↓
        SOUTH (0, +1)
```

---

## Test Patterns

### Offset Testing
```java
int[] offset = EntranceSide.NORTH.getOffset();
assertArrayEquals(new int[]{0, -1}, offset);
```

### Coordinate Testing
```java
int[] coords = EntranceSide.SOUTH.getEntranceCoords(10, 20);
assertArrayEquals(new int[]{10, 21}, coords);
```

### Distinctness Testing
```java
int[][] offsets = {
    EntranceSide.NORTH.getOffset(),
    EntranceSide.EAST.getOffset(),
    EntranceSide.SOUTH.getOffset(),
    EntranceSide.WEST.getOffset()
};

for (int i = 0; i < offsets.length; i++) {
    for (int j = i + 1; j < offsets.length; j++) {
        assertFalse(
            offsets[i][0] == offsets[j][0] && offsets[i][1] == offsets[j][1],
            "Offsets should be distinct"
        );
    }
}
```

---

## Coverage

### Line Coverage
- **Target**: 100%
- **Actual**: 100% (enum is simple)
- **Rationale**: All methods fully tested

### Branch Coverage
- **Switch Statement**: All cases tested (NORTH/EAST/SOUTH/WEST)
- **Edge Cases**: Origin position tested

---

## Test Design Decisions

1. **Why test each direction separately?**: Clear failure messages; easy to identify which direction is wrong.

2. **Why test distinctness?**: Ensures enum values are properly defined; catches copy-paste errors.

3. **Why test at origin?**: Validates no special handling for negative coordinates.

4. **Why array equality?**: Offsets/coords are arrays; need deep equality check.

---

## Potential Test Enhancements

### Future Tests (if enum extended)
- Diagonal directions (NE, NW, SE, SW)
- Rotation logic (e.g., rotate clockwise)
- Opposite direction calculation
- Random direction selection

---

## Related Test Classes

- **Structure Tests**: Test structures with various entrance sides
- **StructurePlacementRules Tests**: Test entrance clearance validation
- **RoadGenerator Tests**: Test roads connecting to entrances

---

## Running Tests

### Run All EntranceSide Tests
```bash
mvn test -Dtest=EntranceSideTest
```

### Run Specific Test
```bash
mvn test -Dtest=EntranceSideTest#testGetEntranceCoordsSouth
```

---

## References

- Source: `EntranceSide.java`
- Design: `BUILD_PHASE1.10.x.md` → Phase 1.10.2
- Related Docs: `EntranceSide.md`, `Structure.md`, `StructurePlacementRules.md`
