# PlacementErrorType

**Package:** `org.adventure.structure`  
**Type:** Enum  
**Phase:** 1.10.2  
**Status:** ✅ Complete

## Overview

Types of errors that can occur during structure placement validation. Used by `StructurePlacementRules` to provide detailed error messages.

## Values

### TOO_CLOSE_TO_STRUCTURE
- **Rule:** Minimum 5-tile spacing (center to center)
- **Reason:** Prevents overlapping structures
- **Fix:** Move further from existing structure

### BLOCKING_ENTRANCE
- **Rule:** Entrance tile must be clear or road
- **Reason:** NPCs must access building from entrance
- **Fix:** Rotate structure or change entrance side

### ON_ROAD
- **Rule:** Structure cannot occupy road tile
- **Reason:** Roads provide movement paths
- **Fix:** Place adjacent to road (entrance can touch)
- **Exception:** Entrance can face road

### UNSUITABLE_TERRAIN
- **Rule:** Elevation must be 0.2-0.7 (unless special)
- **Reason:** Can't build in water or on mountains
- **Fix:** Find flatter, drier location
- **Exceptions:** DOCK, FISHING_HUT can be in water

### OUT_OF_BOUNDS
- **Rule:** Position must be within world dimensions
- **Reason:** Prevent invalid coordinates
- **Fix:** Move within 0 ≤ x < width, 0 ≤ y < height

## Usage

```java
// Check error type
PlacementError error = errors.get(0);

switch (error.getType()) {
    case TOO_CLOSE_TO_STRUCTURE:
        System.out.println("Move further away");
        break;
    case UNSUITABLE_TERRAIN:
        System.out.println("Find flatter ground");
        break;
    // ... other cases
}
```

## Severity

| Type | Severity | Recoverable? |
|------|----------|--------------|
| OUT_OF_BOUNDS | Fatal | No (invalid coords) |
| TOO_CLOSE_TO_STRUCTURE | High | Yes (move away) |
| BLOCKING_ENTRANCE | Medium | Yes (rotate) |
| UNSUITABLE_TERRAIN | Medium | Yes (relocate) |
| ON_ROAD | Low | Yes (shift) |

## Related Classes

- `PlacementError` — Error instance with message
- `StructurePlacementRules` — Validation logic

## References

- **Design Doc:** `BUILD_PHASE1.10.x.md` → Phase 1.10.2
