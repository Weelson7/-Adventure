# RoadTileTest

**Package:** `org.adventure` (test)  
**Type:** JUnit 5 Test Class  
**Tests:** RoadTile data model

---

## Overview

`RoadTileTest` validates the `RoadTile` data model, including Builder pattern, upgrade logic, traffic management, and validation rules. Ensures roads behave correctly under all conditions.

---

## Test Categories

### Builder Pattern Tests (3 tests)

#### `testRoadTileBuilder()`
- **Purpose**: Validate Builder creates RoadTile with all fields
- **Approach**: Set all fields explicitly, verify getters
- **Assertions**: x=10, y=20, type=DIRT, traffic=25, auto=true

#### `testRoadTileBuilderDefaults()`
- **Purpose**: Verify Builder defaults for optional fields
- **Approach**: Set only required fields
- **Expected Defaults**: type=DIRT, traffic=0, auto=true

#### `testRoadTileBuilderPosition()`
- **Purpose**: Test convenience `position(x, y)` method
- **Approach**: Use position() instead of x()/y()
- **Assertions**: x=15, y=25 set correctly

---

### Traffic Management Tests (3 tests)

#### `testIncrementTraffic()`
- **Purpose**: Validate traffic increment logic
- **Approach**: Start at 0, increment twice
- **Assertions**: 0 → 10 → 35

#### `testIncrementTrafficCap()`
- **Purpose**: Verify traffic level caps at 100
- **Approach**: Start at 95, increment by 10
- **Assertions**: Result is 100 (not 105)

#### `testSetTrafficLevel()` (implied)
- **Purpose**: Validate direct traffic level setting
- **Approach**: Set to specific values
- **Assertions**: Traffic updates correctly

---

### Upgrade Logic Tests (4+ tests)

#### `testTryUpgradeDirtToStone()`
- **Purpose**: Validate DIRT → STONE upgrade at traffic 50
- **Approach**: 
  - Start at traffic 49 → tryUpgrade() → false, still DIRT
  - Increment to 50 → tryUpgrade() → true, now STONE
- **Assertions**: Upgrade occurs at threshold, returns true

#### `testTryUpgradeStoneToStone()` (implied)
- **Purpose**: Validate no upgrade when traffic insufficient
- **Approach**: STONE at traffic 70 → tryUpgrade() → false
- **Assertions**: Remains STONE

#### `testTryUpgradeStoneToPaved()` (implied)
- **Purpose**: Validate STONE → PAVED upgrade at traffic 80
- **Approach**: STONE at traffic 80 → tryUpgrade() → true
- **Assertions**: Upgrades to PAVED

#### `testTryUpgradePavedNoOp()` (implied)
- **Purpose**: Verify PAVED has no further upgrades
- **Approach**: PAVED at traffic 100 → tryUpgrade() → false
- **Assertions**: Remains PAVED

---

### Validation Tests (1+ tests)

#### `testInvalidTrafficLevel()` (implied)
- **Purpose**: Validate traffic level constraints
- **Approach**: Attempt to set traffic < 0 or > 100
- **Expected**: IllegalArgumentException

#### `testNullRoadType()` (implied)
- **Purpose**: Validate type cannot be null
- **Approach**: Set type to null
- **Expected**: IllegalArgumentException

---

## Test Data

### Common Test Values
- **Positions**: (10, 20), (15, 25)
- **Traffic Levels**: 0, 25, 49, 50, 70, 80, 95, 100
- **Road Types**: DIRT, STONE, PAVED
- **Ticks**: 1000

---

## Upgrade Thresholds

### Validated Thresholds
- **DIRT → STONE**: traffic >= 50
- **STONE → PAVED**: traffic >= 80
- **PAVED**: No further upgrade

---

## Test Patterns

### Builder Validation
```java
RoadTile road = new RoadTile.Builder()
    .position(x, y)
    .type(RoadType.DIRT)
    .build();
assertEquals(expected, road.getField());
```

### Upgrade Testing
```java
assertFalse(road.tryUpgrade()); // Below threshold
road.incrementTraffic(amount);
assertTrue(road.tryUpgrade());  // At/above threshold
assertEquals(expectedType, road.getType());
```

### Traffic Cap Testing
```java
road.incrementTraffic(largeAmount);
assertEquals(100, road.getTrafficLevel()); // Capped
```

---

## Coverage

### Line Coverage
- **Target**: 85%+
- **Actual**: High coverage of RoadTile class
- **Gaps**: Exception paths (null checks)

### Branch Coverage
- **Upgrade Logic**: All branches tested (DIRT/STONE/PAVED)
- **Traffic Capping**: Both normal and capped cases
- **Validation**: Boundary conditions

---

## Test Design Decisions

1. **Why test defaults?**: Ensures Builder provides sensible starting values.

2. **Why test traffic cap?**: Critical for game balance; prevents unbounded growth.

3. **Why test upgrade thresholds precisely?**: Upgrade logic is core gameplay mechanic.

4. **Why test Builder fluency?**: Ensures fluent API works as expected.

---

## Related Test Classes

- **RoadGenerator Tests**: Test road network generation
- **Village Tests**: Test village structure management
- **Structure Tests**: Test building placement with roads

---

## Running Tests

### Run All RoadTile Tests
```bash
mvn test -Dtest=RoadTileTest
```

### Run Specific Test
```bash
mvn test -Dtest=RoadTileTest#testTryUpgradeDirtToStone
```

---

## References

- Source: `RoadTile.java`
- Design: `BUILD_PHASE1.10.x.md` → Phase 1.10.2
- Related Docs: `RoadTile.md`, `RoadType.md`
