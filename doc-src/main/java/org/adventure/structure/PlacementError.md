# PlacementError

**Package:** `org.adventure.structure`  
**Type:** Immutable Data Class

---

## Overview

`PlacementError` represents an error encountered during structure placement validation. Contains error type and detailed message for debugging and user feedback. Used by `StructurePlacementRules` to report why a structure cannot be placed.

---

## Design Principles

1. **Type + Message**: Structured error with category and details
2. **Immutability**: Error cannot be modified after creation
3. **Validation**: Ensures error data is valid at construction
4. **User-Friendly**: Messages provide actionable feedback

---

## Class Structure

```java
public final class PlacementError {
    private final PlacementErrorType type;
    private final String message;
    
    public PlacementError(PlacementErrorType type, String message);
}
```

---

## Key Methods

### Creation
- **Constructor**: `new PlacementError(type, message)`
  - `type`: Cannot be null
  - `message`: Cannot be null or empty
  - Validates at construction time

### Accessors
- **`getType()`**: Get error type (PlacementErrorType enum)
- **`getMessage()`**: Get detailed error message

### String Representation
- **`toString()`**: Format as "TYPE: message"
  - Example: "TOO_CLOSE_TO_STRUCTURE: Structure at (10, 20) too close to ..."

---

## Usage Examples

### Create a Placement Error
```java
PlacementError error = new PlacementError(
    PlacementErrorType.TOO_CLOSE_TO_STRUCTURE,
    "Structure at (10, 20) too close to existing structure 'House' at (12, 22) - distance 2.8 < 5 tiles"
);
```

### Check Error Type
```java
if (error.getType() == PlacementErrorType.OUT_OF_BOUNDS) {
    // Handle out of bounds error
}
```

### Display Error to User
```java
System.err.println("Placement failed: " + error.toString());
// Output: "TOO_CLOSE_TO_STRUCTURE: Structure at (10, 20) too close to ..."
```

### Collect All Errors
```java
List<PlacementError> errors = rules.validatePlacement(x, y, type, entrance, structures, roads);
for (PlacementError error : errors) {
    System.err.println(error);
}
```

---

## Error Messages

### Format Guidelines
- **Include coordinates**: Show both structure and conflict locations
- **Show thresholds**: Display limits (e.g., "distance 2.8 < 5 tiles")
- **Name entities**: Reference structure IDs/names when relevant
- **Be specific**: State exactly what rule was violated

### Example Messages

**TOO_CLOSE_TO_STRUCTURE**:
```
"Structure at (10, 20) too close to existing structure 'House' at (12, 22) - distance 2.8 < 5 tiles"
```

**BLOCKING_ENTRANCE**:
```
"Structure at (10, 20) has entrance at (10, 21) blocked by existing structure"
```

**ON_ROAD**:
```
"Structure at (10, 20) would be placed on existing road"
```

**UNSUITABLE_TERRAIN**:
```
"Structure at (10, 20) on mountain terrain (elevation 0.85 > 0.70)"
"Structure at (10, 20) in water (elevation 0.15 < 0.20)"
```

**OUT_OF_BOUNDS**:
```
"Structure at (100, 200) is out of world bounds (80 x 80)"
```

---

## Validation Rules

### At Construction
- `type` cannot be null → throws IllegalArgumentException
- `message` cannot be null → throws IllegalArgumentException
- `message` cannot be empty → throws IllegalArgumentException

---

## Related Classes

- **PlacementErrorType**: Enum of error categories
- **StructurePlacementRules**: Creates PlacementError objects during validation
- **Structure**: Subject of placement validation

---

## Testing

**Test Classes**: Covered by `StructurePlacementRules` tests  
**Coverage**: Implicit through validation tests

### Test Scenarios
- Error creation with valid data
- Validation failure (null type/message)
- String formatting
- Error collection and reporting

---

## Design Decisions

1. **Why immutable?**: Errors represent specific validation failures; no reason to modify after creation.

2. **Why type + message?**: Type allows programmatic handling; message provides human-readable details.

3. **Why no exception?**: Placement validation may find multiple errors; list is more useful than throwing on first error.

4. **Why detailed messages?**: Helps players understand what went wrong and how to fix it.

5. **Why final class?**: No need for extension; keeps design simple.

---

## Future Enhancements (Post-MVP)

1. **Localization**: Support multiple languages for error messages
2. **Severity Levels**: Warning vs. blocking errors
3. **Suggested Fixes**: Include advice on how to resolve error
4. **Error Codes**: Numeric codes for easier lookup/documentation
5. **Context Data**: Structured fields for programmatic access to coordinates/IDs

---

## References

- Design: `BUILD_PHASE1.10.x.md` → Phase 1.10.2
- Related: `PlacementErrorType.md`, `StructurePlacementRules.md`
- Usage: Returned by `StructurePlacementRules.validatePlacement()`
