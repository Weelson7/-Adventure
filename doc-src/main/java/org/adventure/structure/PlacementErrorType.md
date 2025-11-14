# PlacementErrorType

**Package:** `org.adventure.structure`  
**Type:** Enum

---

## Overview

`PlacementErrorType` defines the categories of errors that can occur during structure placement validation. Used by `StructurePlacementRules` to classify placement failures and by `PlacementError` to categorize error messages.

---

## Enum Values

### TOO_CLOSE_TO_STRUCTURE
- **Description**: Structure is too close to another structure
- **Rule**: Minimum spacing of 5 tiles from center to center
- **Example**: Building at (10, 20) within 5 tiles of existing building at (12, 22)

### BLOCKING_ENTRANCE
- **Description**: Structure would block another structure's entrance
- **Rule**: Entrance tiles must remain clear (can be road, not structure)
- **Example**: Placing structure at coordinates that block existing entrance tile

### ON_ROAD
- **Description**: Structure cannot be placed on an existing road tile
- **Rule**: Structure center cannot overlap road (entrance can face/touch road)
- **Example**: Attempting to build on a road tile coordinate

### UNSUITABLE_TERRAIN
- **Description**: Terrain is unsuitable for building
- **Reasons**:
  - Elevation too high (> 0.7) - mountains
  - Elevation too low (< 0.2) - water (unless special structure)
- **Example**: Trying to build on mountain peak or in lake

### OUT_OF_BOUNDS
- **Description**: Structure position is outside world bounds
- **Rule**: Structure coordinates must be within world dimensions
- **Example**: Attempting to build at (100, 200) in an 80x80 world

---

## Error Severity

All placement errors in Phase 1.10.2 are **blocking** - structure cannot be placed if any error exists.

### Critical Errors (stop validation)
- **OUT_OF_BOUNDS**: Cannot validate further rules if position is invalid

### Standard Errors (continue validation)
- All other error types allow validation to continue
- Multiple errors can be reported for a single placement attempt

---

## Usage Examples

### Check Error Type
```java
PlacementError error = ...;
if (error.getType() == PlacementErrorType.TOO_CLOSE_TO_STRUCTURE) {
    // Handle proximity error
}
```

### Switch on Error Type
```java
switch (error.getType()) {
    case TOO_CLOSE_TO_STRUCTURE:
        // Suggest finding more space
        break;
    case BLOCKING_ENTRANCE:
        // Suggest moving or changing entrance side
        break;
    case ON_ROAD:
        // Suggest building off the road
        break;
    case UNSUITABLE_TERRAIN:
        // Suggest finding flatter/drier ground
        break;
    case OUT_OF_BOUNDS:
        // Suggest building within world limits
        break;
}
```

### Filter Errors by Type
```java
List<PlacementError> errors = rules.validatePlacement(...);
boolean hasTerrainIssues = errors.stream()
    .anyMatch(e -> e.getType() == PlacementErrorType.UNSUITABLE_TERRAIN);
```

### Count Errors by Type
```java
Map<PlacementErrorType, Long> errorCounts = errors.stream()
    .collect(Collectors.groupingBy(PlacementError::getType, Collectors.counting()));
```

---

## Validation Order

`StructurePlacementRules` checks errors in this order:

1. **OUT_OF_BOUNDS** (fatal, stops validation)
2. **UNSUITABLE_TERRAIN** (elevation checks)
3. **ON_ROAD** (road collision)
4. **TOO_CLOSE_TO_STRUCTURE** (proximity to other structures)
5. **BLOCKING_ENTRANCE** (entrance clearance)

---

## Error Prevention

### TOO_CLOSE_TO_STRUCTURE
- **Prevention**: Use spatial index to find nearby structures before placement
- **Recommendation**: Build at least 6+ tiles apart for safety margin

### BLOCKING_ENTRANCE
- **Prevention**: Check entrance tiles of nearby structures
- **Recommendation**: Leave 2-tile buffer around existing entrances

### ON_ROAD
- **Prevention**: Query road network before choosing placement
- **Recommendation**: Build off roads; entrance can face road

### UNSUITABLE_TERRAIN
- **Prevention**: Sample elevation map before placement
- **Recommendation**: Target elevation range [0.2, 0.7] for most structures

### OUT_OF_BOUNDS
- **Prevention**: Validate coordinates against world dimensions
- **Recommendation**: Clamp coordinates to valid range before placement

---

## Related Classes

- **PlacementError**: Contains error type and message
- **StructurePlacementRules**: Creates errors of these types
- **Structure**: Subject of placement validation

---

## Testing

**Test Classes**: Covered by `StructurePlacementRules` tests  
**Coverage**: All error types tested in validation scenarios

### Test Scenarios
- Each error type triggered individually
- Multiple errors in single validation
- Error type filtering and counting

---

## Design Decisions

1. **Why five error types?**: Covers all placement rules in Phase 1.10.2; more can be added later.

2. **Why all blocking?**: Simplifies initial implementation; warnings can be added in future phases.

3. **Why OUT_OF_BOUNDS separate?**: Fatal error that prevents further validation; needs special handling.

4. **Why UNSUITABLE_TERRAIN combined?**: Water and mountains are both terrain issues; keeps enum concise.

5. **Why enum not class hierarchy?**: Fixed set of error types; no need for error subclasses.

---

## Future Enhancements (Post-MVP)

1. **Warning Types**: Non-blocking issues (suboptimal placement, aesthetic concerns)
2. **Severity Levels**: CRITICAL, ERROR, WARNING, INFO
3. **Resolution Hints**: Suggested coordinates or actions to fix error
4. **Localized Names**: Internationalization support
5. **Error Grouping**: Related errors (e.g., multiple proximity violations)

---

## References

- Design: `BUILD_PHASE1.10.x.md` → Phase 1.10.2
- Related: `PlacementError.md`, `StructurePlacementRules.md`
- Validation: See `StructurePlacementRules.validatePlacement()`
