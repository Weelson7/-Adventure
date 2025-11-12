# StructureTest

**Package:** `org.adventure`  
**Type:** JUnit 5 Test Class  
**Test Count:** 27 tests  
**Coverage:** 85%+ for structure module

---

## Overview

`StructureTest` validates the `Structure` class and related ownership/permission systems. Tests cover creation, validation, health management, ownership transfer, access control, rooms, upgrades, and equality.

---

## Test Categories

### 1. Creation and Validation (5 tests)

#### `testStructureCreation()`
- Creates basic structure with builder
- Validates all fields set correctly
- Checks `isDestroyed()` and `isDamaged()` return false for full health

#### `testStructureRequiresId()`
- Validates that creating structure without ID throws `IllegalArgumentException`

#### `testStructureRequiresType()`
- Validates that creating structure without type throws `IllegalArgumentException`

#### `testStructureRequiresLocationTileId()`
- Validates that creating structure without location throws `IllegalArgumentException`

#### `testStructureHealthCannotExceedMax()`
- Validates that health > maxHealth throws `IllegalArgumentException`

#### `testStructureHealthCannotBeNegative()`
- Validates that negative health throws `IllegalArgumentException`

---

### 2. Health Management (8 tests)

#### `testTakeDamage()`
- Apply 30 damage to 100 health structure
- Validates health = 70, isDamaged = true
- Validates lastUpdatedTick updated

#### `testTakeDamageCannotGoNegative()`
- Apply 150 damage to 100 health structure
- Validates health clamped to 0
- Validates isDestroyed = true

#### `testTakeDamageRejectsNegativeAmount()`
- Validates negative damage throws `IllegalArgumentException`

#### `testRepair()`
- Repair damaged structure in two steps
- Validates health increases but doesn't exceed max
- Validates lastUpdatedTick updated

#### `testRepairCannotExceedMaxHealth()`
- Over-repair structure
- Validates health clamped to maxHealth

#### `testCannotRepairDestroyedStructure()`
- Attempt to repair structure with health = 0
- Validates throws `IllegalStateException`

#### `testRepairRejectsNegativeAmount()`
- Validates negative repair throws `IllegalArgumentException`

#### `testHealthPercentage()`
- Test health percentage calculation
- Validates returns 0.0 to 1.0 range
- Tests at 75%, 50%, and 0% health

---

### 3. Ownership and Permissions (7 tests)

#### `testOwnerAlwaysHasFullAccess()`
- Create structure with owner
- Validates owner has FULL access automatically

#### `testSetPermission()`
- Set PUBLIC permission to READ
- Validates permission stored correctly
- Validates lastUpdatedTick updated

#### `testOwnerPermissionCannotBeChanged()`
- Attempt to set OWNER permission to READ
- Validates owner remains at FULL

#### `testAccessLevelHierarchy()`
- Set CLAN_MEMBER to MODIFY
- Validates has READ, USE, MODIFY access
- Validates does not have MANAGE, FULL access

#### `testDefaultAccessIsNone()`
- Check undefined role access
- Validates defaults to NONE

#### `testTransferOwnership()`
- Transfer ownership to new character
- Validates ownerId and ownerType updated
- Validates old permissions cleared
- Validates new owner has FULL access

#### `testTransferOwnershipRequiresValidOwner()`
- Attempt transfer with null/empty owner
- Validates throws `IllegalArgumentException`

---

### 4. Collections (4 tests)

#### `testAddRoom()`
- Add room to structure
- Validates room count increases
- Validates room in collection
- Validates lastUpdatedTick updated

#### `testAddRoomRejectsNull()`
- Attempt to add null room
- Validates throws `IllegalArgumentException`

#### `testApplyUpgrade()`
- Apply upgrade to structure
- Validates upgrade count increases
- Validates upgrade in collection
- Validates lastUpdatedTick updated

#### `testApplyUpgradeRejectsNull()`
- Attempt to apply null upgrade
- Validates throws `IllegalArgumentException`

---

### 5. Equality (1 test)

#### `testStructureEquality()`
- Create three structures with different IDs/properties
- Validates equality based on ID only
- Validates hashCode consistent with equals

---

### 6. Structure Types (1 test)

#### `testStructureTypeCategories()`
- Test all category query methods
- Validates each StructureType returns correct category
- Tests all 6 categories (Residential, Military, Commercial, Magical, Ruins, Special)

---

## Test Patterns

### Validation Tests
```java
@Test
public void testStructureRequiresId() {
    assertThrows(IllegalArgumentException.class, () -> {
        new Structure.Builder()
                .type(StructureType.HOUSE)
                .locationTileId("100:200:0")
                .maxHealth(100.0)
                .build();
    });
}
```

### State Change Tests
```java
@Test
public void testTakeDamage() {
    Structure structure = createBasicStructure();
    
    structure.takeDamage(30.0, 100);
    
    assertEquals(70.0, structure.getHealth());
    assertTrue(structure.isDamaged());
    assertEquals(100, structure.getLastUpdatedTick());
}
```

### Permission Tests
```java
@Test
public void testAccessLevelHierarchy() {
    structure.setPermission(AccessRole.CLAN_MEMBER, AccessLevel.MODIFY, 100);
    
    assertTrue(structure.hasAccess(AccessRole.CLAN_MEMBER, AccessLevel.READ));
    assertTrue(structure.hasAccess(AccessRole.CLAN_MEMBER, AccessLevel.MODIFY));
    assertFalse(structure.hasAccess(AccessRole.CLAN_MEMBER, AccessLevel.MANAGE));
}
```

---

## Coverage Analysis

### Covered Scenarios
- ✅ Structure creation with builder
- ✅ Field validation (null checks, range checks)
- ✅ Health management (damage, repair, destroy)
- ✅ Ownership transfer
- ✅ Permission system (set, check, hierarchy)
- ✅ Room and upgrade management
- ✅ Equality and hashing

### Not Covered (Future Tests)
- ⏳ Multi-room interactions
- ⏳ Upgrade effects on structure properties
- ⏳ Integration with TaxationSystem
- ⏳ Concurrent permission modifications

---

## Test Execution

### Run All Structure Tests
```bash
.\maven\mvn\bin\mvn.cmd test -Dtest=StructureTest
```

### Run Specific Test
```bash
.\maven\mvn\bin\mvn.cmd test -Dtest=StructureTest#testTakeDamage
```

### Results
```
[INFO] Running org.adventure.StructureTest
[INFO] Tests run: 27, Failures: 0, Errors: 0, Skipped: 0
```

---

## Quality Gates

- ✅ All 27 tests passing
- ✅ 85%+ line coverage for Structure class
- ✅ Zero failures in CI pipeline
- ✅ No flaky tests (100% deterministic)

---

## Design Decisions

1. **Why builder in tests?**: Matches production usage patterns; tests are documentation.

2. **Why test validation thoroughly?**: Structure creation is error-prone; early detection prevents bugs.

3. **Why test permission hierarchy?**: Access control is security-critical; must be airtight.

4. **Why test equality?**: Structures used in collections; equality must be correct.

---

## Future Test Enhancements

1. **Parameterized Tests**: Test all StructureTypes with same logic
2. **Property-Based Tests**: Fuzz testing for health/permission combinations
3. **Integration Tests**: Structure + Region + Taxation integration
4. **Performance Tests**: Benchmark structure operations for large numbers

---

## References

- Source: `src/main/java/org/adventure/structure/Structure.java`
- Design: `docs/structures_ownership.md`
- Summary: `archive/PHASE_1.5_SUMMARY.md`
