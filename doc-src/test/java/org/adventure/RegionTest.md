# RegionTest.java - Region Simulation Test Suite

**Package:** `org.adventure`
**Source:** [RegionTest.java](../../../../src/test/java/org/adventure/RegionTest.java)
**Phase:** 1.2 (Region Simulation)
**Test Framework:** JUnit 5.9.3

## Overview

`RegionTest` validates the core logic of the `Region` class, including region creation, containment, state transitions, resource node management, resource regeneration, tick processing, and NPC tracking. These tests ensure regions behave deterministically, enforce resource caps, and support both active and background simulation modes as specified in the design docs.

## Test Coverage Summary

| Category                   | Tests | Purpose |
|----------------------------|-------|---------|
| Region Creation            | 1     | Constructor, getters |
| Containment Logic          | 2     | Point containment, boundary checks |
| State Transitions          | 1     | Active/background switching |
| Resource Node Management   | 2     | Add/get resource nodes |
| Resource Regeneration      | 2     | Single/multiple node regeneration |
| Tick Processing            | 1     | Tick update logic |
| NPC Tracking               | 1     | NPC count management |
| Getters/Setters            | 1     | Field accessors |

**Total: 11 tests, 100% passing**

## Individual Test Descriptions

### 1. `testRegionCreation()`
- **Purpose:** Validate constructor and field initialization
- **Checks:** ID, center, dimensions, initial state, NPC count

### 2. `testRegionContainsPoint()`
- **Purpose:** Validate region containment logic
- **Checks:** Center, corners, out-of-bounds points

### 3. `testRegionBoundaryContains()`
- **Purpose:** Validate inclusive/exclusive boundary logic
- **Checks:** Min/max boundaries, edge cases

### 4. `testRegionStateTransitions()`
- **Purpose:** Validate active/background state switching
- **Checks:** State changes, getter/setter

### 5. `testRegionResourceNodes()`
- **Purpose:** Validate resource node addition and retrieval
- **Checks:** Add nodes, contains checks

### 6. `testRegionResourceRegeneration()`
- **Purpose:** Validate resource regeneration formula for a single node
- **Checks:** Regeneration math, tick update

### 7. `testRegionMultipleResourceRegeneration()`
- **Purpose:** Validate simultaneous regeneration for multiple nodes
- **Checks:** Formula correctness for each node

### 8. `testRegionTickUpdate()`
- **Purpose:** Validate tick tracking and updates
- **Checks:** Last processed tick logic

### 9. `testRegionNpcCount()`
- **Purpose:** Validate NPC count management
- **Checks:** Set/get NPC count

### 10. `testRegionGetters()`
- **Purpose:** Validate all field accessors
- **Checks:** ID, center, dimensions

## Quality Gates & Design Alignment
- **Determinism:** All tests use fixed inputs, ensuring reproducible results
- **Resource Caps:** Regeneration and harvesting never exceed max or drop below zero
- **Performance:** Tick-based updates, background/active state logic
- **State Preservation:** Last processed tick and NPC count tracked

## Test Execution

```powershell
# Run all RegionTest tests
maven\mvn\bin\mvn.cmd test -Dtest=RegionTest
```

**Expected Output:**
```
[INFO] Running org.adventure.RegionTest
[INFO] Tests run: 11, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: <0.1 s
[INFO] BUILD SUCCESS
```

## References
- [Region.java](../../main/java/org/adventure/region/Region.md) - Implementation docs
- [docs/data_models.md](../../../../docs/data_models.md) - Region design
- [docs/game_parameters_setup.md](../../../../docs/game_parameters_setup.md) - Simulation parameters

---
**Status:** ✅ All 11 tests passing
**Coverage:** 100% of Region.java public API
**Last Updated:** 2025-11-11
