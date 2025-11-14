# VillageTest

**Package:** `org.adventure` (test)  
**Type:** JUnit 5 Test Class  
**Tests:** Village data model

---

## Overview

`VillageTest` validates the `Village` data model, including Builder pattern, field validation, structure management, and mutable property updates. Ensures villages behave correctly under all conditions.

---

## Test Categories

### Builder Pattern Tests (3 tests)

#### `testVillageBuilder()`
- **Purpose**: Validate Builder creates Village with all fields
- **Approach**: Set all fields explicitly, verify getters
- **Assertions**:
  - id="village_1"
  - name="Meadowdale"
  - type=VILLAGE
  - center=(100, 200)
  - structureIds=[3 structures]
  - population=25
  - governingClanId="clan_1"
  - foundedTick=1000

#### `testVillageBuilderRequiredFields()`
- **Purpose**: Verify Builder rejects missing required fields
- **Approach**: Omit id or name, attempt build()
- **Expected**: IllegalArgumentException

#### `testVillageBuilderValidation()`
- **Purpose**: Test Builder validation for invalid values
- **Approach**: Try empty id, negative population, negative tick
- **Expected**: IllegalArgumentException for each case

---

### Structure Management Tests (3+ tests)

#### `testAddStructure()`
- **Purpose**: Validate adding structures to village
- **Approach**: Create village, add structure, verify list
- **Assertions**: Structure ID appears in getStructureIds()

#### `testRemoveStructure()` (implied)
- **Purpose**: Validate removing structures from village
- **Approach**: Create village, remove structure, verify list
- **Assertions**: Structure ID removed from getStructureIds()

#### `testAddDuplicateStructure()` (implied)
- **Purpose**: Verify duplicate structures not added
- **Approach**: Add same structure ID twice
- **Assertions**: Structure list size doesn't change

---

### Mutable Property Tests (4 tests)

#### `testSetName()` (implied)
- **Purpose**: Validate name can be changed
- **Approach**: Create village, setName(), verify
- **Assertions**: getName() returns new name
- **Validation**: Null/empty name rejected

#### `testSetType()` (implied)
- **Purpose**: Validate type can be changed (promotion)
- **Approach**: Create VILLAGE, setType(TOWN), verify
- **Assertions**: getType() returns TOWN
- **Validation**: Null type rejected

#### `testSetPopulation()` (implied)
- **Purpose**: Validate population updates
- **Approach**: Create village, setPopulation(), verify
- **Assertions**: getPopulation() returns new value
- **Validation**: Negative population rejected

#### `testSetGoverningClanId()` (implied)
- **Purpose**: Validate clan ID can be changed
- **Approach**: Create village, setGoverningClanId(), verify
- **Assertions**: getGoverningClanId() returns new ID
- **Validation**: Null allowed (no governing clan)

---

### Equality and Hashing Tests (2 tests)

#### `testEquals()` (implied)
- **Purpose**: Verify equality based on ID only
- **Approach**: Create two villages with same ID
- **Assertions**: equals() returns true

#### `testHashCode()` (implied)
- **Purpose**: Verify hash code based on ID only
- **Approach**: Create two villages with same ID
- **Assertions**: hashCode() returns same value

---

### Edge Cases (1+ tests)

#### `testEmptyStructureList()` (implied)
- **Purpose**: Verify village requires at least one structure
- **Approach**: Build village with empty structure list
- **Expected**: IllegalArgumentException

#### `testImmutableStructureList()` (implied)
- **Purpose**: Verify getStructureIds() returns defensive copy
- **Approach**: Modify returned list
- **Assertions**: Village's internal list unchanged

---

## Test Data

### Common Test Values
- **IDs**: "village_1", "village_2"
- **Names**: "Meadowdale", "Stonefield"
- **Types**: VILLAGE, TOWN, CITY
- **Centers**: (100, 200), (150, 250)
- **Structures**: ["structure_1", "structure_2", "structure_3"]
- **Population**: 0, 25, 50, 100
- **Clans**: "clan_1", "clan_2", null
- **Ticks**: 1000, 2000

---

## Validation Rules Tested

### Required Fields
- `id` cannot be null or empty
- `name` cannot be null or empty
- `type` cannot be null
- `structureIds` must contain at least one structure

### Value Constraints
- `population` cannot be negative
- `foundedTick` cannot be negative

### Immutability
- Structure list returned is defensive copy
- Center coordinates cannot be changed after creation

---

## Test Patterns

### Builder Validation
```java
Village village = new Village.Builder()
    .id("village_1")
    .name("Meadowdale")
    .type(VillageType.VILLAGE)
    .centerX(100)
    .centerY(200)
    .addStructure("struct_1")
    .build();
assertEquals(expected, village.getField());
```

### Required Field Testing
```java
assertThrows(IllegalArgumentException.class, () -> {
    new Village.Builder()
        .name("Test")
        // Missing id
        .build();
});
```

### Mutable Property Testing
```java
village.setPopulation(50);
assertEquals(50, village.getPopulation());

assertThrows(IllegalArgumentException.class, () -> {
    village.setPopulation(-10); // Invalid
});
```

---

## Coverage

### Line Coverage
- **Target**: 85%+
- **Actual**: High coverage of Village class
- **Gaps**: Edge cases in validation

### Branch Coverage
- **Validation Logic**: All branches tested
- **Mutable Setters**: Both valid and invalid cases
- **Builder Pattern**: All paths covered

---

## Test Design Decisions

1. **Why test required fields?**: Ensures data integrity at construction time.

2. **Why test mutable properties?**: Villages evolve; changes must be validated.

3. **Why test defensive copies?**: Prevents external modification of internal state.

4. **Why test equality on ID?**: Villages are unique entities identified by ID.

---

## Related Test Classes

- **VillageManager Tests**: Test village detection and management
- **Structure Tests**: Test buildings that comprise villages
- **RoadTile Tests**: Test roads connecting village structures

---

## Running Tests

### Run All Village Tests
```bash
mvn test -Dtest=VillageTest
```

### Run Specific Test
```bash
mvn test -Dtest=VillageTest#testVillageBuilder
```

---

## References

- Source: `Village.java`
- Design: `BUILD_PHASE1.10.x.md` → Phase 1.10.2
- Related Docs: `Village.md`, `VillageType.md`, `VillageManager.md`
