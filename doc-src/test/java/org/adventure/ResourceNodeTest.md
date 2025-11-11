# ResourceNodeTest.java - Resource Node Test Suite

**Package:** `org.adventure`
**Source:** [ResourceNodeTest.java](../../../../src/test/java/org/adventure/ResourceNodeTest.java)
**Phase:** 1.2 (Region Simulation)
**Test Framework:** JUnit 5.9.3

## Overview

`ResourceNodeTest` validates the logic for resource nodes, including creation, regeneration, harvesting, depletion, type handling, and formula determinism. These tests ensure resource nodes behave according to the capped regeneration formula, support finite/infinite resources, and enforce all design constraints from the economy and simulation docs.

## Test Coverage Summary

| Category                   | Tests | Purpose |
|----------------------------|-------|---------|
| Node Creation              | 1     | Constructor, getters |
| Regeneration Formula       | 4     | Standard, near-max, capped, zero-recovery |
| Harvesting Logic           | 2     | Standard, insufficient resources |
| Harvest/Regeneration Cycle | 1     | Combined cycle correctness |
| Finite Resource Handling   | 1     | Zero regen for finite types |
| Depletion/Full Flags       | 2     | isDepleted, isFull logic |
| Quantity Clamping          | 1     | Clamp to [0, Rmax] |
| Resource Type Coverage     | 1     | All five types tested |
| Formula Determinism        | 1     | Same inputs → same outputs |

**Total: 14 tests, 100% passing**

## Individual Test Descriptions

### 1. `testResourceNodeCreation()`
- **Purpose:** Validate constructor and field initialization
- **Checks:** ID, type, coordinates, Rmax, regen rate, initial quantity

### 2-5. Regeneration Formula Tests
- **Purpose:** Validate capped regeneration formula
- **Checks:** Standard, near-max, capped, zero-recovery cases

### 6-7. Harvesting Logic Tests
- **Purpose:** Validate harvesting and depletion
- **Checks:** Standard harvest, insufficient resources

### 8. Harvest/Regeneration Cycle
- **Purpose:** Validate combined harvest and regeneration
- **Checks:** Correct quantity after cycle

### 9. Finite Resource Handling
- **Purpose:** Validate zero regen for finite resources (ORE, STONE)

### 10-11. Depletion/Full Flags
- **Purpose:** Validate isDepleted and isFull logic

### 12. Quantity Clamping
- **Purpose:** Validate clamping to [0, Rmax]

### 13. Resource Type Coverage
- **Purpose:** Validate all five resource types

### 14. Formula Determinism
- **Purpose:** Validate deterministic regeneration

## Quality Gates & Design Alignment
- **Capped Regeneration:** Formula: `R(t+1) = R(t) + regenRate * dt * (1 - R(t)/Rmax)`
- **Resource Caps:** Never exceeds Rmax or drops below zero
- **Finite/Infinite Types:** ORE/STONE do not regenerate
- **Determinism:** Same inputs always produce same outputs

## Test Execution

```powershell
# Run all ResourceNodeTest tests
maven\mvn\bin\mvn.cmd test -Dtest=ResourceNodeTest
```

**Expected Output:**
```
[INFO] Running org.adventure.ResourceNodeTest
[INFO] Tests run: 14, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: <0.1 s
[INFO] BUILD SUCCESS
```

## References
- [ResourceNode.java](../../main/java/org/adventure/region/ResourceNode.md) - Implementation docs
- [docs/economy_resources.md](../../../../docs/economy_resources.md) - Resource formulas
- [docs/data_models.md](../../../../docs/data_models.md) - Resource node design

---
**Status:** ✅ All 14 tests passing
**Coverage:** 100% of ResourceNode.java public API
**Last Updated:** 2025-11-11
