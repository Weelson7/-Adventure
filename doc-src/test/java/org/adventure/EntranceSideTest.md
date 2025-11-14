# EntranceSideTest

**Package:** `org.adventure`  
**Type:** Unit Test  
**Phase:** 1.10.2  
**Status:** ✅ Complete  
**Coverage:** 10 tests, 100% line coverage

## Overview

Tests for `EntranceSide` enum. Validates offset calculations and entrance coordinate generation for all four entrance directions (NORTH, EAST, SOUTH, WEST).

## Test Categories

### Offset Tests (4 tests)
- `testNorthOffset()` — NORTH = (0, -1)
- `testEastOffset()` — EAST = (+1, 0)
- `testSouthOffset()` — SOUTH = (0, +1)
- `testWestOffset()` — WEST = (-1, 0)

### Entrance Coordinate Tests (5 tests)
- `testGetEntranceCoordsNorth()` — North entrance from (10, 20) = (10, 19)
- `testGetEntranceCoordsEast()` — East entrance from (10, 20) = (11, 20)
- `testGetEntranceCoordsSouth()` — South entrance from (10, 20) = (10, 21)
- `testGetEntranceCoordsWest()` — West entrance from (10, 20) = (9, 20)
- `testGetEntranceCoordsAtOrigin()` — Entrance from (0, 0) works correctly

### Validation Tests (1 test)
- `testAllDirectionsDistinct()` — All four offsets are unique

## Key Tests

### testSouthOffset
```java
@Test
public void testSouthOffset() {
    int[] offset = EntranceSide.SOUTH.getOffset();
    assertArrayEquals(new int[]{0, 1}, offset);
}
```

### testGetEntranceCoordsSouth
```java
@Test
public void testGetEntranceCoordsSouth() {
    int[] coords = EntranceSide.SOUTH.getEntranceCoords(10, 20);
    assertArrayEquals(new int[]{10, 21}, coords);  // South means +Y
}
```

### testAllDirectionsDistinct
```java
@Test
public void testAllDirectionsDistinct() {
    int[][] offsets = {
        EntranceSide.NORTH.getOffset(),
        EntranceSide.EAST.getOffset(),
        EntranceSide.SOUTH.getOffset(),
        EntranceSide.WEST.getOffset()
    };
    
    // Verify all offsets are different
    for (int i = 0; i < offsets.length; i++) {
        for (int j = i + 1; j < offsets.length; j++) {
            assertFalse(
                offsets[i][0] == offsets[j][0] && offsets[i][1] == offsets[j][1],
                "EntranceSide offsets should be distinct"
            );
        }
    }
}
```

## Offset Reference

| Direction | Offset (dx, dy) | From (10, 20) | Result (x, y) |
|-----------|-----------------|---------------|---------------|
| NORTH     | (0, -1)         | (10, 20)      | (10, 19)      |
| EAST      | (+1, 0)         | (10, 20)      | (11, 20)      |
| SOUTH     | (0, +1)         | (10, 20)      | (10, 21)      |
| WEST      | (-1, 0)         | (10, 20)      | (9, 20)       |

## Test Results

```
[INFO] Running org.adventure.EntranceSideTest
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
```

**Success Rate:** 100%  
**Execution Time:** < 1 second

## Related Classes

- `EntranceSide` — Enum under test
- `Structure` — Uses EntranceSide
- `RoadGenerator` — Uses entrance coordinates
- `StructurePlacementRules` — Validates entrance clearance

## References

- **Source:** `src/main/java/org/adventure/structure/EntranceSide.java`  
- **Design:** `BUILD_PHASE1.10.x.md` → Phase 1.10.2
