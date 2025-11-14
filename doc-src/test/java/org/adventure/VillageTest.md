# VillageTest

**Package:** `org.adventure`  
**Type:** Unit Test  
**Phase:** 1.10.2  
**Status:** ✅ Complete  
**Coverage:** 13 tests, 100% line coverage

## Overview

Tests for `Village` data model. Validates Builder pattern, field validation, structure management, and mutable field operations.

## Test Categories

### Builder Pattern (3 tests)
- `testVillageBuilder()` — Validate complete construction
- `testVillageBuilderRequiredFields()` — Missing required fields throw exceptions
- `testVillageBuilderValidation()` — Invalid values (empty id, negative population) rejected

### Structure Management (4 tests)
- `testAddStructure()` — Add structures to village
- `testAddStructureNoDuplicates()` — Prevent duplicate structure IDs
- `testRemoveStructure()` — Remove structures from village
- `testRemoveNonexistentStructure()` — Safely handle missing structures

### Mutable Fields (3 tests)
- `testSetName()` — Update village name with validation
- `testSetPopulation()` — Update population (validates >= 0)
- `testSetType()` — Promote/demote village type

### Metadata (1 test)
- `testSchemaVersion()` — Default schema version is 1

## Key Tests

### testVillageBuilder
```java
@Test
public void testVillageBuilder() {
    Village village = new Village.Builder()
        .id("village_1")
        .name("Meadowdale")
        .type(VillageType.VILLAGE)
        .centerX(100)
        .centerY(200)
        .structureIds(Arrays.asList("structure_1", "structure_2", "structure_3"))
        .population(25)
        .governingClanId("clan_1")
        .foundedTick(1000)
        .build();
    
    assertEquals("village_1", village.getId());
    assertEquals("Meadowdale", village.getName());
    assertEquals(VillageType.VILLAGE, village.getType());
    assertEquals(100, village.getCenterX());
    assertEquals(200, village.getCenterY());
    assertEquals(3, village.getStructureIds().size());
    assertEquals(25, village.getPopulation());
    assertEquals("clan_1", village.getGoverningClanId());
    assertEquals(1000, village.getFoundedTick());
}
```

### testAddStructureNoDuplicates
```java
@Test
public void testAddStructureNoDuplicates() {
    Village village = new Village.Builder()
        .id("village_1")
        .name("Testville")
        .type(VillageType.VILLAGE)
        .centerX(0)
        .centerY(0)
        .structureIds(new ArrayList<>(Arrays.asList("initial_structure")))
        .build();
    
    village.addStructure("structure_1");
    village.addStructure("structure_1");  // Duplicate
    
    assertEquals(2, village.getStructureIds().size());  // initial + structure_1
}
```

### testSetName
```java
@Test
public void testSetName() {
    Village village = /* ... */;
    
    village.setName("NewName");
    assertEquals("NewName", village.getName());
    
    assertThrows(IllegalArgumentException.class, () -> {
        village.setName(null);
    });
    
    assertThrows(IllegalArgumentException.class, () -> {
        village.setName("");
    });
}
```

## Validation Tests

### Required Fields
- `id` — Cannot be null/empty
- `name` — Cannot be null/empty
- `type` — Cannot be null
- `structureIds` — Must have at least 1 element (enforced by constructor)

### Value Constraints
- `population` — Cannot be negative
- `foundedTick` — Cannot be negative

## Test Results

```
[INFO] Running org.adventure.VillageTest
[INFO] Tests run: 13, Failures: 0, Errors: 0, Skipped: 0
```

**Success Rate:** 100%  
**Execution Time:** < 1 second

## Related Classes

- `Village` — Class under test
- `VillageType` — Enum used in tests
- `VillageManager` — (Planned) Integration tests

## References

- **Source:** `src/main/java/org/adventure/settlement/Village.java`  
- **Design:** `BUILD_PHASE1.10.x.md` → Phase 1.10.2
