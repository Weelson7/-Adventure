# PlacementError

**Package:** `org.adventure.structure`  
**Type:** Error Class  
**Phase:** 1.10.2  
**Status:** ✅ Complete

## Overview

Represents an error encountered during structure placement validation. Contains error type and detailed message for debugging/user feedback.

## Purpose

- **Validation Feedback:** Explain why structure placement failed
- **User-Friendly:** Provides descriptive error messages
- **Type Classification:** Categorizes errors by PlacementErrorType

## Data Model

```java
public class PlacementError {
    PlacementErrorType type;  // Error category
    String message;            // Detailed description
}
```

## API Reference

### Constructor
```java
public PlacementError(PlacementErrorType type, String message)
```

**Validation:**
- `type` cannot be null
- `message` cannot be null or empty

### Getters
```java
public PlacementErrorType getType()
public String getMessage()
```

### toString
```java
public String toString()
```

**Returns:** `"<TYPE>: <message>"` format

**Example:** `"TOO_CLOSE_TO_STRUCTURE: Structure at (10, 20) too close to 'house_1' at (12, 21)"`

## Usage

### Placement Validation
```java
StructurePlacementRules rules = /* ... */;

List<PlacementError> errors = rules.validatePlacement(
    x, y, type, entrance, existingStructures, roadTiles);

if (!errors.isEmpty()) {
    System.out.println("Cannot place structure:");
    for (PlacementError error : errors) {
        System.out.println("  - " + error);
    }
}
```

### Error Types
```java
for (PlacementError error : errors) {
    switch (error.getType()) {
        case TOO_CLOSE_TO_STRUCTURE:
            // Suggest moving further away
            break;
        case BLOCKING_ENTRANCE:
            // Rotate structure or change entrance
            break;
        case UNSUITABLE_TERRAIN:
            // Find flatter location
            break;
        case OUT_OF_BOUNDS:
            // Move within world bounds
            break;
        case ON_ROAD:
            // Shift slightly to avoid road
            break;
    }
}
```

## Error Examples

### TOO_CLOSE_TO_STRUCTURE
```
"Structure at (10, 20) too close to existing structure 'house_1' at (12, 21) - distance 2.2 < 5 tiles"
```

### BLOCKING_ENTRANCE
```
"Structure at (10, 20) has entrance at (10, 21) blocked by existing structure"
```

### ON_ROAD
```
"Structure at (10, 20) would be placed on existing road"
```

### UNSUITABLE_TERRAIN
```
"Structure at (10, 20) on mountain terrain (elevation 0.85 > 0.70)"
"Structure at (10, 20) in water (elevation 0.15 < 0.20)"
```

### OUT_OF_BOUNDS
```
"Structure at (300, 400) is out of world bounds (256 x 256)"
```

## Related Classes

- `PlacementErrorType` — Error type enum
- `StructurePlacementRules` — Generates PlacementError instances

## References

- **Design Doc:** `BUILD_PHASE1.10.x.md` → Phase 1.10.2
